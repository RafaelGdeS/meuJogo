package Jogo;

import java.util.Scanner;

public class Jogo {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.print("Deseja arrancar como (1) Servidor ou (2) Cliente? ");
        String op = sc.nextLine().trim();

        if (op.equals("1")) {
            GameServer.main(new String[0]);
        } else if (op.equals("2")) {
            GameClient.main(new String[0]);
        } else {
            System.out.println("Opção desconhecida.");
        }
    }
}