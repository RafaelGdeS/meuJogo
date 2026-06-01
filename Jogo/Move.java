package Jogo;

import java.util.Objects;

public class Move {
    public final boolean horizontal;
    public final int row;
    public final int col;

    public Move(boolean horizontal, int row, int col) {
        this.horizontal = horizontal;
        this.row = row;
        this.col = col;
    }

    public boolean isHorizontal() { return horizontal; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return horizontal == move.horizontal &&
                row == move.row &&
                col == move.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(horizontal, row, col);
    }

    @Override
    public String toString() {
        return (horizontal ? "H" : "V") + "(" + row + "," + col + ")";
    }
}