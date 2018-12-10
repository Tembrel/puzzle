package net.peierls.puzzle.pegs;

import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.util.BitSet;
import java.util.Set;

import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;


/**
 * Utilities for encoding peg puzzles as bit vectors and calculating
 * possible jumps in those terms.
 */
class PegEncoding {
    /*
     * Encoding description
     *
     * . means hole no peg
     * X means hole with peg
     * | is used for delimiting beginning and end of rows
     * _ is used for spacer in the bit vector form (no hole or peg)
     *
     * Here is a 4x4 cross with 4 pegs (holes in every position except the corners):
     *
     * | .. |
     * |.XX.|
     * |..XX|
     * | .. |
     *
     * Using origin 0 (row, col) coordinates:
     *
     * - There is one possible rightward jump, from (1, 1) to (1, 3) over (1, 2).
     * - There are two leftward jumps, from (1, 2) to (1, 0) over (1, 1) and
     *   from (2, 3) to (2, 1) over (2, 2).
     *
     * We can get a flat representation of a grid as a bit vector in row major
     * or column major order, using two positions as spacers between rows
     * or columns, respectively. We need the spacers because we'll be doing
     * logical shifts of up to 2, and we don't want bits to move between rows
     * or columns.
     *
     * To map a position (r, c) in an (m x n) grid to its row major vector position:
     *
     * (r, c) => r * (n+2) + c, and the fence index is m*(n+2) - 1.
     *
     * To map back:
     *
     * i => (i / (n+2), i % (n+2))
     *
     * To map the same grid point to its vector position in column major order:
     *
     * (r, c) => c * (m+2) + r, and the fence index is n*(m+2) - 1.
     *
     * To map back:
     *
     * j => (j / (m+2), j % (m+2))
     *
     * Here is the row major vector for the 4x4 cross above, with spacers between rows,
     * followed by a breakdown into bitsets (digits with superscore are row indices,
     * otherwise col indices):
     * _     _     _     _
     * 00123 10123 20123 30123
     * | .. __.XX.__..XX__ .. |
     * |0110001111001111000110| holes ([.X] is 1, [ _] is 0)
     * |0000000110000011000000| pegs (X is 1, anything else 0)
     * |1111111001111100111111| ~pegs
     * |0110001001001100000110| holes & ~pegs
     *
     * Peg positions where there is a peg one to the right,
     * i.e., positions where pegs is true and pegs
     * shifted *left* one is true:
     *
     *   |0000000110000011000000| pegs
     * & |0000001100000110000000| (pegs << 1)
     *   ========================
     *   |0000000100000010000000| pegs & (pegs << 1)
     *
     * Peg positions with a hole but no peg two to the right,
     * i.e., positions where pegs is true and (holes&~pegs)
     * shifted *left* two is true.
     *
     *   |0000000110000011000000| pegs
     * & |1000100100110000011000| (holes & ~pegs) << 2
     *   ========================
     *   |0000000100000000000000| pegs & ((holes & ~pegs) << 2)
     *
     * Anding together both of these yields the rightmost jump starting positions:
     *
     *   |0000000100000010000000| pegs & (pegs << 1)
     * & |0000000100000000000000| pegs & ((holes & ~pegs) << 2)
     *   ========================
     *   |0000000100000000000000| pegs & (pegs << 1) & ((holes & ~pegs) << 2)
     *   _     _     _     _
     *   00123 10123 20123 30123
     *
     * Only one possible rightward jump from (1, 1), as described above.
     *
     * Leftward jumps use the same formula, but using *right* logical shifts:
     *
     *   |0000000110000011000000| pegs
     * & |0000000011000001100000| (pegs >>> 1)
     *   ========================
     *   |0000000010000001000000| pegs & (pegs >>> 1)
     *
     *   |0000000110000011000000| pegs
     * & |0001100010010011000001| (holes & ~pegs) >>> 2
     *   ========================
     *   |0000000010000011000000| pegs & ((holes & ~pegs) >>> 2)
     *
     *   |0000000010000001000000| pegs & (pegs >>> 1)
     * & |0000000010000011000000| pegs & ((holes & ~pegs) >>> 2)
     *   ========================
     *   |0000000010000001000000| pegs & (pegs >>> 1) & ((holes & ~pegs) >>> 2)
     *   _     _     _     _
     *   00123 10123 20123 30123
     *
     * Two possible leftward jumps, from (1, 2) and (2, 3).
     *
     * For downward and upward jumps, use the rightward and leftward
     * jump formulas, respectively, on the column major bit vector.
     */

    enum Major {
        ROW,
        COL
    }

    /**
     * Types of jump: rightward, leftward, downward, upward.
     */
    enum JumpType {
        RIGHT(Major.ROW, 0, 2, 0, 1),
        LEFT(Major.ROW, 0, -2, 0, -1),
        DOWN(Major.COL, 2, 0, 1, 0),
        UP(Major.COL, -2, 0, -1, 0),
        ;

        final Major major;
        final Position targetDelta;
        final Position jumpedDelta;

        JumpType(Major major, int tdr, int tdc, int jdr, int jdc) {
            this.major = major;
            this.targetDelta = new Position(tdr, tdc);
            this.jumpedDelta = new Position(jdr, jdc);
        }
        Position target(Position source) {
            return source.move(targetDelta);
        }
        Position jumped(Position source) {
            return source.move(jumpedDelta);
        }
        int targetShift() {
            switch (major) {
                default:
                case ROW: return -targetDelta.col();
                case COL: return -targetDelta.row();
            }
        }
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
    }

    private static final int PAD = 2;

    final int nrows;
    final int ncols;
    final int rowMajorFence;
    final int colMajorFence;

    PegEncoding(int nrows, int ncols) {
        this.nrows = nrows;
        this.ncols = ncols;
        this.rowMajorFence = nrows * (ncols + PAD) - 1;
        this.colMajorFence = ncols * (nrows + PAD) - 1;
    }

    int toRowMajor(Position pos) {
        return toRowMajor(pos.row(), pos.col());
    }

    int toRowMajor(int row, int col) {
        return row * (ncols + PAD) + col;
    }

    int toColMajor(Position pos) {
        return toColMajor(pos.row(), pos.col());
    }

    int toColMajor(int row, int col) {
        return col * (nrows + PAD) + row;
    }

    Position fromRowMajor(int i) {
        return new Position(i / (ncols + PAD), i % (ncols + PAD));
    }

    Position fromColMajor(int j) {
        return new Position(j / (nrows + PAD), j % (nrows + PAD));
    }

    BitSet toRowMajor(Set<Position> positions) {
        return StreamEx.of(positions)
            .mapToInt(this::toRowMajor)
            .toBitSet();
    }

    BitSet toColMajor(Set<Position> positions) {
        return StreamEx.of(positions)
            .mapToInt(this::toColMajor)
            .toBitSet();
    }

    ImmutableSet<Position> fromRowMajor(BitSet bits) {
        return IntStreamEx.of(bits)
            .mapToObj(this::fromRowMajor)
            .collect(toImmutableSet());
    }

    ImmutableSet<Position> fromColMajor(BitSet bits) {
        return IntStreamEx.of(bits)
            .mapToObj(this::fromColMajor)
            .collect(toImmutableSet());
    }

    BitSet transposeRowToCol(BitSet rowMajorBits) {
        return toColMajor(fromRowMajor(rowMajorBits));
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

    EntryStream<JumpType, Position> legalJumps(BitSet rmHoles, BitSet rmPegs) {
        BitSet cmHoles = transposeRowToCol(rmHoles);
        BitSet cmPegs = transposeRowToCol(rmPegs);

        BitSet rmHolesOnly = (BitSet) rmHoles.clone();
        rmHolesOnly.andNot(rmPegs);

        BitSet cmHolesOnly = (BitSet) cmHoles.clone();
        cmHolesOnly.andNot(cmPegs);

        return StreamEx.of(JumpType.values())
            .mapToEntry(jump -> {
                BitSet holes, pegs, holesOnly;
                int fence;
                switch (jump.major) {
                    default:
                    case ROW:
                        return fromRowMajor(jump.legal(rmHoles, rmPegs, rmHolesOnly, rowMajorFence));
                    case COL:
                        return fromColMajor(jump.legal(cmHoles, cmPegs, cmHolesOnly, colMajorFence));
                }
            })
            .flatMapValues(StreamEx::of);
    }
}
