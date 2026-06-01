package Jogo;

import java.util.Scanner;

public class JogoTeste {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Bem-vindo ao Dots & Boxes!");
        System.out.print("Tamanho do tabuleiro (ex: 3): ");
        int size = Integer.parseInt(sc.nextLine().trim());
        Game game = new Game(size);

        while (!game.isFinished()) {
            game.printState();
            System.out.println("Jogada de " + game.getCurrentPlayer() + " (Ex: h 1 0 para horizontal, linha 1, coluna 0)");
            
            System.out.print("horiz/vert (h/v): ");
            String h = sc.nextLine().trim().toLowerCase();
            boolean horizontal = h.startsWith("h");
            System.out.print("Linha: ");
            int row = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Coluna: ");
            int col = Integer.parseInt(sc.nextLine().trim());

            Move move = new Move(horizontal, row, col);
            game.playMove(move);
        }
        game.printState();
        game.printWinner();
    }
}