package Jogo;

public class Game {
    private final Board board;
    private char currentPlayer = 'A';
    private int scoreA = 0;
    private int scoreB = 0;

    public Game(int size) {
        board = new Board(size);
    }

    public void playMove(Move move) {
        if (!board.isValidMove(move)) {
            System.out.println("Jogada inválida! Coordenadas fora do tabuleiro ou já usada.");
            return;
        }

        int boxes = board.applyMove(move, currentPlayer);

        if (currentPlayer == 'A') scoreA += boxes;
        else scoreB += boxes;

        if (boxes == 0) switchPlayer();
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 'A') ? 'B' : 'A';
    }

    public boolean isFinished() {
        return board.isFull();
    }

    public void printState() {
        board.printBoard();
        System.out.println("Pontuação -> A: " + scoreA + " | B: " + scoreB);
        System.out.println("Jogador atual: " + currentPlayer);
    }

    public void printWinner() {
        if (scoreA > scoreB) {
            System.out.println("Jogador A venceu!");
        } else if (scoreB > scoreA) {
            System.out.println("Jogador B venceu!");
        } else {
            System.out.println("Empate!");
        }
    }

    public Board getBoard() {
        return board;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }
}