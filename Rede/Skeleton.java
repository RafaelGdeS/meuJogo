package Rede;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Skeleton {
    private final int porto;
    private final String nome;
    private ServerSocket servidorSocket;
    private volatile boolean ativo = true;

    private final Map<String, MetodoRemoto> metodosDisponiveis = new HashMap<>();

    public Skeleton(int porto, String nome) {
        this.porto = porto;
        this.nome = nome;
        registarMetodosDisponiveis();
    }

    private void registarMetodosDisponiveis() {
        metodosDisponiveis.put("ECHO", (args) -> {
            if (args.length == 0) return "ERRO: ECHO requer argumento.";
            return "ECHO: " + String.join(" ", args);
        });

        metodosDisponiveis.put("TEMPO", (args) -> {
            return "TEMPO: " + System.currentTimeMillis();
        });

        metodosDisponiveis.put("LISTAR_METODOS", (args) -> {
            StringBuilder sb = new StringBuilder("METODOS_DISPONIVEIS:\n");
            for (String metodo : metodosDisponiveis.keySet()) {
                sb.append("  - ").append(metodo).append("\n");
            }
            return sb.toString();
        });

        metodosDisponiveis.put("SOMA", (args) -> {
            if (args.length < 2) return "ERRO: SOMA requer 2 números.";
            try {
                int a = Integer.parseInt(args[0]);
                int b = Integer.parseInt(args[1]);
                return "SOMA: " + (a + b);
            } catch (NumberFormatException e) {
                return "ERRO: Argumentos inválidos.";
            }
        });

        metodosDisponiveis.put("MULTIPLICACAO", (args) -> {
            if (args.length < 2) return "ERRO: MULTIPLICACAO requer 2 números.";
            try {
                int a = Integer.parseInt(args[0]);
                int b = Integer.parseInt(args[1]);
                return "MULTIPLICACAO: " + (a * b);
            } catch (NumberFormatException e) {
                return "ERRO: Argumentos inválidos.";
            }
        });
    }

    public void iniciar() throws IOException {
        servidorSocket = new ServerSocket(porto);
        servidorSocket.setReuseAddress(true);
        System.out.println("=== SKELETON INICIADO ===");
        System.out.println("Nome: " + nome);
        System.out.println("Porto: " + porto);
        System.out.println("Métodos disponíveis: " + metodosDisponiveis.size());

        while (ativo) {
            try {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("[CLIENTE CONECTADO] " + clienteSocket.getInetAddress().getHostAddress());
                new Thread(() -> tratarRequisicao(clienteSocket)).start();
            } catch (SocketException e) {
                if (ativo) System.out.println("Erro ao aceitar cliente: " + e.getMessage());
            }
        }
    }

    private void tratarRequisicao(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String linha;
            while ((linha = in.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String resposta = processarRequisicao(linha);
                out.println(resposta);

                if (linha.equalsIgnoreCase("SAIR")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao tratar requisição: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            System.out.println("[CLIENTE DESCONECTADO]");
        }
    }

    private String processarRequisicao(String requisicao) {
        String[] partes = requisicao.split("\\s+");
        if (partes.length == 0) return "ERRO: Requisição vazia.";

        String nomeMetodo = partes[0].toUpperCase();
        String[] argumentos = Arrays.copyOfRange(partes, 1, partes.length);

        MetodoRemoto metodo = metodosDisponiveis.get(nomeMetodo);
        if (metodo == null) {
            return "ERRO: Método '" + nomeMetodo + "' não encontrado.";
        }

        try {
            return metodo.executar(argumentos);
        } catch (Exception e) {
            return "ERRO ao executar método: " + e.getMessage();
        }
    }

    public void parar() {
        ativo = false;
        try {
            if (servidorSocket != null && !servidorSocket.isClosed()) {
                servidorSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar skeleton: " + e.getMessage());
        }
        System.out.println("Skeleton parado.");
    }

    @FunctionalInterface
    private interface MetodoRemoto {
        String executar(String[] argumentos) throws Exception;
    }

    public static void main(String[] args) throws IOException {
        int porto = args.length > 0 ? Integer.parseInt(args[0]) : 6000;
        String nome = args.length > 1 ? args[1] : "SkeletonPadrao";

        Skeleton skeleton = new Skeleton(porto, nome);
        Runtime.getRuntime().addShutdownHook(new Thread(skeleton::parar));
        skeleton.iniciar();
    }
}