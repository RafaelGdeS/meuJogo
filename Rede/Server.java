package Rede;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(20);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor à escuta em: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente: " + clientSocket.getInetAddress());
                pool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("ERRO servidor: " + e.getMessage());
        }
        pool.shutdown();
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("Bem-vindo ao servidor! Escreve 'sair' para terminar.");
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Recebido: " + line);
                if (line.trim().equalsIgnoreCase("sair")) {
                    out.println("Adeus!");
                    break;
                } else {
                    out.println("Eco: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro com cliente: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
            System.out.println("Cliente desconectado.");
        }
    }
}