package Jogo;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public final class GameServer {
    private static final int DEFAULT_PORT = 5025;
    private static final Object lock = new Object();
    private static UserStore store;
    private static final Deque<ClientConn> queue = new ArrayDeque<>();
    private static Match match;

    public static void main(String[] args) throws IOException {
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        int size = args.length >= 2 ? Integer.parseInt(args[1]) : 3;
        store = new UserStore("users.db");

        try (ServerSocket server = new ServerSocket(port)) {
            server.setReuseAddress(true);
            System.out.println("GameServer a escutar na porta " + port + " (tabuleiro " + size + "x" + size + ")");
            while (true) {
                Socket s = server.accept();
                System.out.println("Conexão aceite: " + s.getRemoteSocketAddress());
                new Thread(() -> handleClient(s, size)).start();
            }
        }
    }

    private static void handleClient(Socket socket, int size) {
        ClientConn me = null;

        try (socket) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            me = new ClientConn(out);

            out.println("INFO Bem-vindo. Use REGISTER ou LOGIN.");
            out.println("INFO Comandos disponíveis:");
            out.println("INFO REGISTER <nick> <pass> <age> <nationality> <image>");
            out.println("INFO LOGIN <nick> <pass>");
            out.println("INFO Depois de login: PROFILE | UPDATE_IMAGE <image> | QUEUE | MOVE <true/false> <row> <col> | /quit");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("/quit")) {
                    out.println("OK Adeus!");
                    break;
                }
                if (line.startsWith("REGISTER ")) {
                    handleRegister(me, line);
                    continue;
                }
                if (line.startsWith("LOGIN ")) {
                    handleLogin(me, line);
                    continue;
                }
                if (me.nickname == null) {
                    out.println("ERRO: Tem de fazer LOGIN primeiro.");
                    continue;
                }
                if (line.equals("PROFILE")) {
                    out.println(store.profileLine(me.nickname));
                    continue;
                }
                if (line.startsWith("UPDATE_IMAGE ")) {
                    String img = line.substring("UPDATE_IMAGE ".length()).trim();
                    handleUpdateImage(me, img);
                    continue;
                }
                if (line.equals("QUEUE")) {
                    handleQueue(me, size);
                    continue;
                }
                if (line.startsWith("MOVE ")) {
                    handleMove(me, line);
                    continue;
                }
                out.println("ERRO: Comando desconhecido.");
            }

        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        } finally {
            synchronized (lock) {
                queue.remove(me);

                if (match != null && (match.a == me || match.b == me)) {
                    broadcast(match, "INFO Um jogador desconectou. Jogo cancelado!");
                    match = null;
                }
            }
            System.out.println("DESCONECTADO: " + (me == null ? "" : me.nickname));
        }
    }

    private static void handleRegister(ClientConn me, String line) {
        String[] parts = line.split("\\s+", 6);
        if (parts.length != 6) {
            me.out.println("ERRO Uso: REGISTER <nick> <pass> <age> <nationality> <image>");
            return;
        }
        String nick = parts[1];
        String pass = parts[2];
        int age;
        try { age = Integer.parseInt(parts[3]); }
        catch (Exception e) { me.out.println("ERRO A idade tem de ser número inteiro."); return; }

        String nat = parts[4];
        String img = parts[5];
        try {
            User u = store.register(nick, pass, img, nat, age);
            if (u == null) {
                me.out.println("ERRO Nickname já existe.");
            } else {
                me.out.println("OK Registado. Agora faça LOGIN " + nick + " <pass>");
            }
        } catch (IOException e) {
            me.out.println("ERRO Não foi possível guardar users.db: " + e.getMessage());
        }
    }

    private static void handleLogin(ClientConn me, String line) {
        String[] parts = line.split("\\s+");
        if (parts.length != 3) {
            me.out.println("ERRO Uso: LOGIN <nick> <pass>");
            return;
        }
        String nick = parts[1];
        String pass = parts[2];

        synchronized (lock) {
            if (match != null && (nick.equals(match.nickA) || nick.equals(match.nickB))) {
                me.out.println("ERRO Utilizador já está em jogo.");
                return;
            }
            for (ClientConn c : queue) {
                if (nick.equals(c.nickname)) {
                    me.out.println("ERRO Utilizador já está em espera.");
                    return;
                }
            }
        }
        User u = store.login(nick, pass);
        if (u == null) {
            me.out.println("ERRO Nickname ou password inválidos.");
            return;
        }
        me.nickname = nick;
        me.out.println("OK Login efetuado como " + nick);
        me.out.println(store.profileLine(nick));
    }

    private static void handleUpdateImage(ClientConn me, String img) {
        try {
            boolean ok = store.updateImage(me.nickname, img);
            if (!ok) me.out.println("ERRO Utilizador inexistente.");
            else me.out.println("OK Imagem atualizada.");
        } catch (IOException e) {
            me.out.println("ERRO Não foi possível guardar users.db: " + e.getMessage());
        }
    }

    private static void handleQueue(ClientConn me, int size) {
        synchronized (lock) {
            if (match != null) {
                me.out.println("ERRO Já está a decorrer um jogo. Tente mais tarde.");
                return;
            }
            if (queue.contains(me)) {
                me.out.println("ERRO Já está na fila.");
                return;
            }
            queue.addLast(me);
            me.out.println("OK Em fila de espera. Aguarde adversário...");
            if (queue.size() >= 2) {
                ClientConn a = queue.removeFirst();
                ClientConn b = queue.removeFirst();

                match = new Match(a, b, size);
                a.out.println("MATCH_START A adversário=" + b.nickname);
                b.out.println("MATCH_START B adversário=" + a.nickname);

                broadcast(match, renderState(match));
            }
        }
    }

    private static void handleMove(ClientConn me, String line) {
        synchronized (lock) {
            if (match == null) {
                me.out.println("ERRO Nenhum jogo ativo. Use QUEUE.");
                return;
            }
            if (me != match.a && me != match.b) {
                me.out.println("ERRO Não está no jogo ativo.");
                return;
            }

            char myRole = (me == match.a) ? 'A' : 'B';
            char current = match.game.getCurrentPlayer();
            if (myRole != current) {
                me.out.println("ERRO Não é o seu turno. Turno de: " + current);
                return;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 4) {
                me.out.println("ERRO Uso: MOVE <true/false> <row> <col>");
                return;
            }
            boolean h;
            int r, c;
            try {
                h = Boolean.parseBoolean(parts[1]);
                r = Integer.parseInt(parts[2]);
                c = Integer.parseInt(parts[3]);
            } catch (Exception ex) {
                me.out.println("ERRO Valores MOVE inválidos.");
                return;
            }
            Move mv = new Move(h, r, c);

            if (!match.game.getBoard().isValidMove(mv)) {
                me.out.println("ERRO Jogada inválida (fora do tabuleiro ou repetida).");
                return;
            }

            match.game.playMove(mv);
            broadcast(match, renderState(match));

            if (match.game.isFinished()) {
                endMatchAndPersistLocked();
            }
        }
    }

    private static void endMatchAndPersistLocked() {
        Match m = match;
        if (m == null) return;
        long end = System.currentTimeMillis();
        long duration = end - m.startMillis;
        String winnerText = captureWinnerText(m.game);

        broadcast(m, "GAME_OVER duraçãoMs=" + duration);
        broadcast(m, "WINNER " + winnerText);

        boolean aWin = winnerText.contains("Jogador A venceu");
        boolean bWin = winnerText.contains("Jogador B venceu");
        boolean draw = winnerText.contains("Empate");

        try {
            if (draw) {
                store.recordGameResult(m.nickA, false, duration);
                store.recordGameResult(m.nickB, false, duration);
            } else {
                store.recordGameResult(m.nickA, aWin, duration);
                store.recordGameResult(m.nickB, bWin, duration);
            }
        } catch (IOException e) {
            broadcast(m, "ERRO Não foi possível guardar users.db: " + e.getMessage());
        }
        match = null;
    }

    private static void broadcast(Match m, String msg) {
        m.a.out.println(msg);
        m.b.out.println(msg);
    }

    private static String renderState(Match m) {
        PrintStream original = System.out;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            System.setOut(ps);
            m.game.printState();
            ps.flush();

            return "STATE\n" +
                   baos.toString("UTF-8").trim() +
                   "\nTURN " + m.game.getCurrentPlayer() +
                   "\nENDSTATE";
        } catch (Exception e) {
            return "ERRO a renderizar estado: " + e.getMessage();
        } finally {
            System.setOut(original);
        }
    }

    private static String captureWinnerText(Game g) {
        PrintStream original = System.out;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
            System.setOut(ps);
            g.printWinner();
            ps.flush();
            return baos.toString("UTF-8").trim();
        } catch (Exception e) {
            return "ERRO a determinar vencedor: " + e.getMessage();
        } finally {
            System.setOut(original);
        }
    }

    private static final class ClientConn {
        final PrintWriter out;
        String nickname;

        ClientConn(PrintWriter out) {
            this.out = out;
        }
    }

    private static final class Match {
        final ClientConn a;
        final ClientConn b;
        final String nickA;
        final String nickB;
        final Game game;
        final long startMillis;
        final Instant startedAt;

        Match(ClientConn a, ClientConn b, int size) {
            this.a = a;
            this.b = b;
            this.nickA = a.nickname;
            this.nickB = b.nickname;
            this.game = new Game(size);
            this.startMillis = System.currentTimeMillis();
            this.startedAt = Instant.now();
        }
    }
}