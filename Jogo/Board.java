package Jogo;

public class Board {
    private final int size;
    private final boolean[][] hLines;
    private final boolean[][] vLines;
    private final char[][] boxes;

    public Board(int size) {
        this.size = size;
        hLines = new boolean[size][size - 1];
        vLines = new boolean[size - 1][size];
        boxes = new char[size - 1][size - 1];
    }

    public boolean isValidMove(Move move) {
        if (move.horizontal) {
            if (move.row < 0 || move.row >= size ||
                move.col < 0 || move.col >= size - 1) {
                return false;
            }
            return !hLines[move.row][move.col];
        } else {
            if (move.row < 0 || move.row >= size - 1 ||
                move.col < 0 || move.col >= size) {
                return false;
            }
            return !vLines[move.row][move.col];
        }
    }

    public int applyMove(Move move, char symbol) {
        if (!isValidMove(move)) return -1;

        if (move.horizontal) {
            hLines[move.row][move.col] = true;
        } else {
            vLines[move.row][move.col] = true;
        }

        return checkBoxes(symbol);
    }

    private int checkBoxes(char symbol) {
        int count = 0;

        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1; j++) {

                if (boxes[i][j] == '\0' &&
                    hLines[i][j] &&
                    hLines[i + 1][j] &&
                    vLines[i][j] &&
                    vLines[i][j + 1]) {

                    boxes[i][j] = symbol;
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isFull() {
        for (boolean[] row : hLines)
            for (boolean b : row)
                if (!b) return false;

        for (boolean[] row : vLines)
            for (boolean b : row)
                if (!b) return false;

        return true;
    }

    public void printBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print("*");

                if (j < size - 1) {
                    System.out.print(hLines[i][j] ? "---" : "   ");
                }
            }
            System.out.println();

            if (i < size - 1) {
                for (int j = 0; j < size; j++) {
                    System.out.print(vLines[i][j] ? "|" : " ");

                    if (j < size - 1) {
                        char c = boxes[i][j];
                        System.out.print(c == '\0' ? "   " : " " + c + " ");
                    }
                }
                System.out.println();
            }
        }
    }
}