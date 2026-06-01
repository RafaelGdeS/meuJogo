package Rede;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class P2Peer {
    private final GestorNos gestorNos = new GestorNos();
    private final int portoLocal;

    public P2Peer(int portoLocal) {
        this.portoLocal = portoLocal;
    }

    public void start() {
        new Thread(this::servidorPeer).start();

        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n--- Menu P2P ---");
                System.out.println("1) Ligar a peer");
                System.out.println("2) Listar peers conhecidos");
                System.out.println("3) Enviar mensagem a todos");
                System.out.println("4) Sair");
                System.out.print("Opção: ");

                String op = sc.nextLine().trim();
                switch (op) {
                    case "1" -> ligarAPeer(sc);
                    case "2" -> listarPeers();
                    case "3" -> enviarMensagemATodos(sc);
                    case "4" -> {
                        System.out.println("A terminar...");
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            }
        }
    }

    private void ligarAPeer(Scanner sc) {
        System.out.print("IP do peer: ");
        String ip = sc.nextLine().trim();
        System.out.print("Porto do peer: ");
        int port = Integer.parseInt(sc.nextLine().trim());

        if (gestorNos.adicionarNo(ip, port)) {
            System.out.println("Peer adicionado.");
            try (
                Socket sock = new Socket(ip, port);
                PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))
            ) {
                out.println("LISTA_PEERS");
                String resposta = in.readLine();
                if (resposta != null && resposta.startsWith("PEERS:")) {
                    String[] peers = resposta.substring(6).split(";");
                    for (String peer : peers) {
                        String[] hostPort = peer.split(":");
                        if (hostPort.length == 2) {
                            try {
                                String pIp = hostPort[0];
                                int pPort = Integer.parseInt(hostPort[1]);
                                gestorNos.adicionarNo(pIp, pPort);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Não foi possível ligar ou obter peers: " + e.getMessage());
            }
        } else {
            System.out.println("Esse peer já estava na lista.");
        }
    }

    private void listarPeers() {
        List<NoRemoto> lista = gestorNos.listarNos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum peer conhecido.");
        } else {
            System.out.println("Peers conhecidos:");
            for (NoRemoto n : lista) {
                System.out.println(" - " + n);
            }
        }
    }

    private void enviarMensagemATodos(Scanner sc) {
        System.out.print("Mensagem: ");
        String msg = sc.nextLine();
        for (NoRemoto peer : gestorNos.listarNos()) {
            try (
                Socket socket = new Socket(peer.ip, peer.port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                out.println("MENSAGEM " + msg);
            } catch (IOException e) {
                System.out.println("Falha a enviar para " + peer + ": " + e.getMessage());
            }
        }
        System.out.println("Mensagem enviada a todos.");
    }

    private void servidorPeer() {
        try (ServerSocket server = new ServerSocket(portoLocal)) {
            System.out.println("Servidor P2P ativo no porto " + portoLocal);
            while (true) {
                Socket sock = server.accept();
                new Thread(() -> tratarLigacao(sock)).start();
            }
        } catch (IOException e) {
            System.out.println("ERRO no servidor peer: " + e.getMessage());
        }
    }

    private void tratarLigacao(Socket sock) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true)
        ) {
            String linha = in.readLine();
            if (linha == null) return;
            if (linha.equals("LISTA_PEERS")) {
                List<NoRemoto> lista = gestorNos.listarNos();
                StringBuilder resposta = new StringBuilder("PEERS:");
                for (NoRemoto n : lista) {
                    resposta.append(n.ip).append(":").append(n.port).append(";");
                }
                out.println(resposta);
            } else if (linha.startsWith("MENSAGEM ")) {
                String msg = linha.substring(9);
                System.out.println("Mensagem recebida de " + sock.getInetAddress() + ": " + msg);
            } else {
                out.println("Comando desconhecido.");
            }
        } catch (IOException e) {
            System.out.println("Erro a tratar ligação de peer: " + e.getMessage());
        } finally {
            try { sock.close(); } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) {
        int porto = 20000;
        if (args.length > 0) {
            porto = Integer.parseInt(args[0]);
        }
        P2Peer peer = new P2Peer(porto);
        peer.start();
    }
}