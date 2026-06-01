package Rede;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Jogo.Game;
import Jogo.Move;
import Jogo.User;
import Jogo.UserStore;

public class ServidorJogo {
    private static final int PORTO_PADRAO = 5025;
    private static final int TAMANHO_TABULEIRO_PADRAO = 3;

    private final int porto;
    private final int tamanhoTabuleiro;

    private final UserStore userStore;
    private final Map<String, JogadorConectado> jogadoresConectados = new ConcurrentHashMap<>();
    private final Deque<JogadorConectado> filaEspera = new ArrayDeque<>();
    private final Map<String, PartidaAtiva> partidasAtivas = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public ServidorJogo(int porto, int tamanhoTabuleiro) throws IOException {
        this.porto = porto;
        this.tamanhoTabuleiro = tamanhoTabuleiro;
        this.userStore = new UserStore("users.db");
    }

    public void iniciar() throws IOException {
        try (ServerSocket servidor = new ServerSocket(porto)) {
            servidor.setReuseAddress(true);
            System.out.println("=== SERVIDOR DE JOGO INICIADO ===");
            System.out.println("Porta: " + porto);
            System.out.println("Tamanho do tabuleiro: " + tamanhoTabuleiro + "x" + tamanhoTabuleiro);

            while (true) {
                Socket socket = servidor.accept();
                System.out.println("[NOVO CLIENTE] " + socket.getInetAddress().getHostAddress());
                new Thread(() -> tratarJogador(socket)).start();
            }
        }
    }

    private void tratarJogador(Socket socket) {
        JogadorConectado jogador = null;
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            jogador = new JogadorConectado(socket.getInetAddress().getHostAddress(), out);

            out.println("INFO Bem-vindo ao Servidor de Jogo!");
            out.println("INFO Comandos: REGISTER | LOGIN | QUEUE | MOVE | PROFILE | SAIR");

            String linha;
            while ((linha = in.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                if (linha.equalsIgnoreCase("SAIR")) {
                    out.println("OK Adeus!");
                    break;
                }

                if (linha.startsWith("REGISTER ")) {
                    procesarRegister(jogador, linha);
                    continue;
                }

                if (linha.startsWith("LOGIN ")) {
                    procesarLogin(jogador, linha);
                    continue;
                }

                if (jogador.nickname == null) {
                    out.println("ERRO: Faça LOGIN primeiro.");
                    continue;
                }

                if (linha.equals("PROFILE")) {
                    out.println(userStore.profileLine(jogador.nickname));
                    continue;
                }

                if (linha.equals("QUEUE")) {
                    procesarQueue(jogador);
                    continue;
                }

                if (linha.startsWith("MOVE ")) {
                    procesarMove(jogador, linha);
                    continue;
                }

                out.println("ERRO: Comando desconhecido.");
            }

        } catch (IOException e) {
            System.out.println("[ERRO] Ligação perdida: " + e.getMessage());
        } finally {
            if (jogador != null) {
                limparJogador(jogador);
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void procesarRegister(JogadorConectado jogador, String linha) {
        String[] parts = linha.split("\\s+", 6);
        if (parts.length != 6) {
            jogador.out.println("ERRO Uso: REGISTER <nick> <pass> <age> <nationality> <image>");
            return;
        }
        String nick = parts[1];
        String pass = parts[2];
        int age;
        try { age = Integer.parseInt(parts[3]); }
        catch (Exception e) { jogador.out.println("ERRO A idade deve ser número inteiro."); return; }

        String nat = parts[4];
        String img = parts[5];

        try {
            User u = userStore.register(nick, pass, img, nat, age);
            if (u == null) {
                jogador.out.println("ERRO Nickname já existe.");
            } else {
                jogador.out.println("OK Registado com sucesso. Faça LOGIN.");
            }
        } catch (IOException e) {
            jogador.out.println("ERRO Não foi possível guardar dados: " + e.getMessage());
        }
    }

    private void procesarLogin(JogadorConectado jogador, String linha) {
        String[] parts = linha.split("\\s+");
        if (parts.length != 3) {
            jogador.out.println("ERRO Uso: LOGIN <nick> <pass>");
            return;
        }
        String nick = parts[1];
        String pass = parts[2];

        synchronized (lock) {
            if (jogadoresConectados.containsKey(nick)) {
                jogador.out.println("ERRO Utilizador já conectado.");
                return;
            }
        }

        User u = userStore.login(nick, pass);
        if (u == null) {
            jogador.out.println("ERRO Nickname ou password inválidos.");
            return;
        }

        jogador.nickname = nick;
        jogadoresConectados.put(nick, jogador);
        jogador.out.println("OK Login efetuado com sucesso.");
        jogador.out.println(userStore.profileLine(nick));
    }

    private void procesarQueue(JogadorConectado jogador) {
        synchronized (lock) {
            if (jogador.nickname == null) {
                jogador.out.println("ERRO Faça LOGIN primeiro.");
                return;
            }
            if (filaEspera.contains(jogador)) {
                jogador.out.println("ERRO Já está na fila.");
                return;
            }
            filaEspera.addLast(jogador);
            jogador.out.println("OK Em fila de espera...");

            if (filaEspera.size() >= 2) {
                JogadorConectado j1 = filaEspera.removeFirst();
                JogadorConectado j2 = filaEspera.removeFirst();
                iniciarPartida(j1, j2);
            }
        }
    }

    private void procesarMove(JogadorConectado jogador, String linha) {
        synchronized (lock) {
            PartidaAtiva partida = null;
            for (PartidaAtiva p : partidasAtivas.values()) {
                if (p.jogador1 == jogador || p.jogador2 == jogador) {
                    partida = p;
                    break;
                }
            }

            if (partida == null) {
                jogador.out.println("ERRO Não está em nenhuma partida.");
                return;
            }

            char papel = (partida.jogador1 == jogador) ? 'A' : 'B';
            char turnoAtual = partida.jogo.getCurrentPlayer();

            if (papel != turnoAtual) {
                jogador.out.println("ERRO Não é o seu turno.");
                return;
            }

            String[] parts = linha.split("\\s+");
            if (parts.length != 4) {
                jogador.out.println("ERRO Uso: MOVE <true/false> <row> <col>");
                return;
            }

            try {
                boolean horizontal = Boolean.parseBoolean(parts[1]);
                int row = Integer.parseInt(parts[2]);
                int col = Integer.parseInt(parts[3]);
                Move move = new Move(horizontal, row, col);

                if (!partida.jogo.getBoard().isValidMove(move)) {
                    jogador.out.println("ERRO Jogada inválida.");
                    return;
                }

                partida.jogo.playMove(move);
                enviarEstadoPartida(partida);

                if (partida.jogo.isFinished()) {
                    finalizarPartida(partida);
                }
            } catch (Exception e) {
                jogador.out.println("ERRO Valores inválidos: " + e.getMessage());
            }
        }
    }

    private void iniciarPartida(JogadorConectado j1, JogadorConectado j2) {
        String idPartida = UUID.randomUUID().toString();
        PartidaAtiva partida = new PartidaAtiva(j1, j2, tamanhoTabuleiro);
        partidasAtivas.put(idPartida, partida);

        j1.out.println("MATCH_START Jogador A contra " + j2.nickname);
        j2.out.println("MATCH_START Jogador B contra " + j1.nickname);

        enviarEstadoPartida(partida);
    }

    private void enviarEstadoPartida(PartidaAtiva partida) {
        String estado = capturarEstadoJogo(partida.jogo);
        partida.jogador1.out.println(estado);
        partida.jogador2.out.println(estado);
    }

    private String capturarEstadoJogo(Game jogo) {
        PrintStream original = System.out;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            System.setOut(ps);
            jogo.printState();
            ps.flush();
            return "STATE\n" + baos.toString("UTF-8").trim() + "\nENDSTATE";
        } catch (Exception e) {
            return "ERRO ao renderizar estado.";
        } finally {
            System.setOut(original);
        }
    }

    private void finalizarPartida(PartidaAtiva partida) {
        long duracao = System.currentTimeMillis() - partida.inicioMs;
        String vencedor = capturarVencedor(partida.jogo);

        partida.jogador1.out.println("GAME_OVER Duração: " + duracao + "ms");
        partida.jogador2.out.println("GAME_OVER Duração: " + duracao + "ms");
        partida.jogador1.out.println(vencedor);
        partida.jogador2.out.println(vencedor);

        try {
            boolean j1Win = vencedor.contains("Jogador A venceu");
            boolean j2Win = vencedor.contains("Jogador B venceu");
            userStore.recordGameResult(partida.jogador1.nickname, j1Win, duracao);
            userStore.recordGameResult(partida.jogador2.nickname, j2Win, duracao);
        } catch (IOException e) {
            System.out.println("Erro ao guardar stats: " + e.getMessage());
        }

        for (String id : partidasAtivas.keySet()) {
            if (partidasAtivas.get(id) == partida) {
                partidasAtivas.remove(id);
                break;
            }
        }
    }

    private String capturarVencedor(Game jogo) {
        PrintStream original = System.out;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            System.setOut(ps);
            jogo.printWinner();
            ps.flush();
            return baos.toString("UTF-8").trim();
        } catch (Exception e) {
            return "ERRO ao determinar vencedor.";
        } finally {
            System.setOut(original);
        }
    }

    private void limparJogador(JogadorConectado jogador) {
        synchronized (lock) {
            if (jogador.nickname != null) {
                jogadoresConectados.remove(jogador.nickname);
            }
            filaEspera.remove(jogador);

            for (PartidaAtiva partida : partidasAtivas.values()) {
                if (partida.jogador1 == jogador || partida.jogador2 == jogador) {
                    JogadorConectado outro = (partida.jogador1 == jogador) ? partida.jogador2 : partida.jogador1;
                    outro.out.println("INFO Adversário desconectou. Partida cancelada.");
                    partidasAtivas.values().remove(partida);
                    break;
                }
            }
        }
        System.out.println("[DESCONECTADO] " + (jogador.nickname != null ? jogador.nickname : jogador.ip));
    }

    public static void main(String[] args) throws IOException {
        int porto = args.length > 0 ? Integer.parseInt(args[0]) : PORTO_PADRAO;
        int tamanho = args.length > 1 ? Integer.parseInt(args[1]) : TAMANHO_TABULEIRO_PADRAO;

        ServidorJogo servidor = new ServidorJogo(porto, tamanho);
        servidor.iniciar();
    }

    private static class JogadorConectado {
        final String ip;
        final PrintWriter out;
        String nickname;

        JogadorConectado(String ip, PrintWriter out) {
            this.ip = ip;
            this.out = out;
        }
    }

    private static class PartidaAtiva {
        final JogadorConectado jogador1;
        final JogadorConectado jogador2;
        final Game jogo;
        final long inicioMs;

        PartidaAtiva(JogadorConectado j1, JogadorConectado j2, int tamanho) {
            this.jogador1 = j1;
            this.jogador2 = j2;
            this.jogo = new Game(tamanho);
            this.inicioMs = System.currentTimeMillis();
        }
    }
}