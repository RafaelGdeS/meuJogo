package Jogo;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class GameClient {
    private static final int DEFAULT_PORT = 5025;

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {

            System.out.print("Server host [localhost]: ");
            String host = sc.nextLine().trim();
            if (host.isEmpty()) host = "localhost";

            System.out.print("Server port [" + DEFAULT_PORT + "]: ");
            String p = sc.nextLine().trim();
            int port = p.isEmpty() ? DEFAULT_PORT : Integer.parseInt(p);

            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                Thread receiver = new Thread(() -> {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected.");
                    }
                });
                receiver.setDaemon(true);
                receiver.start();

                while (true) {
                    System.out.println("\n--- MENU ---");
                    System.out.println("1) REGISTER");
                    System.out.println("2) LOGIN");
                    System.out.println("3) PROFILE");
                    System.out.println("4) UPDATE_IMAGE");
                    System.out.println("5) QUEUE (start match)");
                    System.out.println("6) MOVE (during match)");
                    System.out.println("0) SAIR");
                    System.out.print("> ");

                    String opt = sc.nextLine().trim();

                    if (opt.equals("0")) {
                        out.println("/quit");
                        break;
                    }

                    switch (opt) {
                        case "1" -> {
                            System.out.print("Nickname (no spaces): ");
                            String nick = sc.nextLine().trim();
                            System.out.print("Password (no spaces): ");
                            String pass = sc.nextLine().trim();
                            System.out.print("Age (int): ");
                            String age = sc.nextLine().trim();
                            System.out.print("Nationality (no spaces): ");
                            String nat = sc.nextLine().trim();
                            System.out.print("Image string (no spaces, can change later): ");
                            String img = sc.nextLine().trim();

                            out.println("REGISTER " + nick + " " + pass + " " + age + " " + nat + " " + img);
                        }
                        case "2" -> {
                            System.out.print("Nickname: ");
                            String nick = sc.nextLine().trim();
                            System.out.print("Password: ");
                            String pass = sc.nextLine().trim();
                            out.println("LOGIN " + nick + " " + pass);
                        }
                        case "3" -> out.println("PROFILE");
                        case "4" -> {
                            System.out.print("New image string (no spaces): ");
                            String img = sc.nextLine().trim();
                            out.println("UPDATE_IMAGE " + img);
                        }
                        case "5" -> out.println("QUEUE");
                        case "6" -> {
                            System.out.print("Horizontal? (true/false): ");
                            boolean h = Boolean.parseBoolean(sc.nextLine().trim());
                            System.out.print("Linha: ");
                            int r = Integer.parseInt(sc.nextLine().trim());
                            System.out.print("Coluna: ");
                            int c = Integer.parseInt(sc.nextLine().trim());
                            out.println("MOVE " + h + " " + r + " " + c);
                        }
                        default -> System.out.println("Invalid option.");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}