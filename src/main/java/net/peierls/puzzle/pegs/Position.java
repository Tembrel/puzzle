package net.peierls.puzzle.pegs;

import com.google.errorprone.annotations.Immutable;

import java.util.Objects;

/**
 * Represents a grid position in a peg puzzle.
 */
@Immutable
class Position {
    private final int row;
    private final int col;

    Position(int row, int col) { this.row = row; this.col = col; }

    public int row() { return row; }
    public int col() { return col; }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position that = (Position) obj;
        return this.row == that.row && this.col == that.col;
    }
    @Override public int hashCode() {
        return Objects.hash(row, col);
    }
    @Override public String toString() {
        return String.format("(%d, %d)", row, col);
    }

    public Position move(Position amount) {
        return new Position(row + amount.row, col + amount.col);
    }
}
