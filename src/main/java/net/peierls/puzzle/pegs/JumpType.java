package net.peierls.puzzle.pegs;

import java.util.BitSet;

import one.util.streamex.IntStreamEx;


/**
 * Computation of jumps in four directions: right, left, down, up.
 * The target of a jump is two holes in the jump direction.
 * The hole jumped over is one hole in the jump direction.
 * Right and left jumps are computed in a row major context,
 * Down and up jumps are computed in a column major context.
 */
enum JumpType {
    RIGHT(Major.ROW, 0, 2, 0, 1, "-", ">"),
    LEFT(Major.ROW, 0, -2, 0, -1, "-", "<"),
    DOWN(Major.COL, 2, 0, 1, 0, "|", "v"),
    UP(Major.COL, -2, 0, -1, 0, "|", "^"),
    ;

    private final Major major;
    private final Position targetDelta;
    private final Position jumpedDelta;
    private final String sourceSymbol;
    private final String jumpedSymbol;

    JumpType(Major major,
            int targetDeltaRow, int targetDeltaCol,
            int jumpedDeltaRow, int jumpedDeltaCol,
            String sourceSymbol, String jumpedSymbol) {
        this.major = major;
        this.targetDelta = new Position(targetDeltaRow, targetDeltaCol);
        this.jumpedDelta = new Position(jumpedDeltaRow, jumpedDeltaCol);
        this.sourceSymbol = sourceSymbol;
        this.jumpedSymbol = jumpedSymbol;
    }

    Major major() { return major; }
    String sourceSymbol() { return sourceSymbol; }
    String jumpedSymbol() { return jumpedSymbol; }

    /**
     * Position of the target jumped to from source.
     */
    Position target(Position source) {
        return source.move(targetDelta);
    }

    /**
     * Position of the peg jumped from source.
     */
    Position jumped(Position source) {
        return source.move(jumpedDelta);
    }

    /**
     * The target shift amount (always of magnitude 2)
     * for a bit vector in this jump's major order.
     * which is the opposite of the jump direction.
     */
    int targetShift() {
        switch (major) {
            default:
            case ROW: return -targetDelta.col();
            case COL: return -targetDelta.row();
        }
    }

    /**
     * The neighbor shift amount (always of magnitude 1)
     * for a bit vector in this jump's major order.
     * which is the opposite of the jump direction.
     */
    int jumpedShift() {
        switch (major) {
            default:
            case ROW: return -jumpedDelta.col();
            case COL: return -jumpedDelta.row();
        }
    }

    BitSet legal(BitSet holes, BitSet pegs, BitSet holesOnly, int fence) {
        BitSet pegsShiftedOne = shift(pegs, jumpedShift(), fence);
        BitSet holesOnlyShiftedTwo = shift(holesOnly, targetShift(), fence);

        BitSet result = (BitSet) pegs.clone();
        result.and(pegsShiftedOne);
        result.and(holesOnlyShiftedTwo);

        return result;
    }

    static BitSet shift(BitSet bits, int shift, int fence) {
        bits = (BitSet) bits.clone();
        if (shift > 0) {
            return IntStreamEx.of(bits).map(b -> b + shift).filter(b -> b < fence).toBitSet();
        } else if (shift < 0) {
            return IntStreamEx.of(bits).map(b -> b + shift).remove(b -> b < 0).toBitSet();
        }
        return bits;
    }
}
