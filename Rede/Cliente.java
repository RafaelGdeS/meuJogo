package Rede;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in)
        ) {
            Thread receiver = new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        System.out.println("[Servidor] " + line);
                    }
                } catch (IOException e) {
                    System.out.println("Ligação fechada pelo servidor.");
                }
            });
            receiver.setDaemon(true);
            receiver.start();

            System.out.println("Ligado ao servidor em " + host + ":" + port);
            System.out.println("Escreve mensagens ou 'sair' para terminar:");

            while (true) {
                String msg = sc.nextLine();
                out.println(msg);

                if (msg.equalsIgnoreCase("sair")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
        System.out.println("Cliente terminado.");
    }
}