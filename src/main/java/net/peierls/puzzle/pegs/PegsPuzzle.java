package net.peierls.puzzle.pegs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
//import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.hash.Funnel;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.peierls.puzzle.DfsPuzzleSolver;
import net.peierls.puzzle.BloomPuzzleStateFilter;
import net.peierls.puzzle.PuzzleSolver;
import net.peierls.puzzle.PuzzleState;

//import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;


/**
 * A peg-jumping puzzle.
 * The puzzle type holds common information about the puzzle, and the
 * {@link PuzzleState} is an inner class of the puzzle type, to allow
 * access to common information from state method implementations.
 *
 * @see <a href="https://www.chiark.greenend.org.uk/~sgtatham/puzzles/doc/pegs.html">Simon
 * Tatham's puzzle page</a>
 */
public final class PegsPuzzle {

    static class Move {
        final JumpType jump;
        final Position from;
        Move(JumpType jump, Position from) {
            this.jump = jump;
            this.from = from;
        }
        Position from() { return from; }
        Position to() { return jump.target(from); }
        Position mid() { return jump.jumped(from); }
        @Override public String toString() {
            return String.format(
                "Move peg at %s %s to %s, removing peg at %s",
                from(), jump, to(), mid()
            );
        }
    }

    public final class State implements PuzzleState<State> {
        private final State pred;
        private final Move move;
        private final BitSet pegs; // row major


        State(Set<Position> pegs) {
            this.pred = null;
            this.move = null;
            this.pegs = encoding.toRowMajor(pegs);
        }

        State(State pred, Move move) {
            this.pred = pred;
            this.move = move;
            this.pegs = pred.applyJump(move);
        }

        @Override public boolean isSolution() {
            return pegs.cardinality() == solutionCount;
        }

        @Override public Stream<State> successors() {
            //System.out.println("Searching from " + this);
            return encoding.legalJumps(rowMajorHoles, pegs)
                .mapKeyValue(Move::new)
                .map(move -> new State(this, move));
        }

        @Override public Optional<State> predecessor() {
            return Optional.ofNullable(pred);
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof State)) return false;
            State that = (State) obj;
            return this.pegs.equals(that.pegs);
        }

        @Override public int hashCode() {
            return Objects.hashCode(pegs);
        }

        @Override
        public String toString() {
            Set<Position> pegs = encoding.fromRowMajor(this.pegs);
            StringBuilder buf = new StringBuilder();
            if (move == null) {
                buf.append("\nInitial position");
            } else {
                buf.append("\n").append(move);
            }
            buf.append(String.format(": #pegs=%d", pegs.size()));
            rowMajorPositions(nrows, ncols).forEach(cur -> {
                if (cur.col() == 0) {
                    buf.append("\n");
                }
                String symbol = null;
                if (move != null) {
                    if (cur.equals(move.from())) {
                        symbol = "-";
                    } else if (cur.equals(move.to())) {
                        symbol = "+";
                    } else if (cur.equals(move.mid())) {
                        symbol = "0";
                    }
                }
                if (symbol == null) {
                    if (pegs.contains(cur)) {
                        symbol = "X";
                    } else if (holes.contains(cur)) {
                        symbol = ".";
                    } else {
                        symbol = " ";
                    }
                }
                buf.append(symbol);
            });
            return buf.toString();
        }

        public Position dimensions() {
            return new Position(nrows, ncols);
        }

        public Set<Position> holes() {
            return holes;
        }

        private BitSet applyJump(Move move) {
            BitSet newPegs = (BitSet) pegs.clone();
            //System.out.printf("applying %s to %s:%n", move, pegs);
            StreamEx.of(move.from(), move.to(), move.mid())
                .mapToInt(encoding::toRowMajor)
                //.peek(b -> System.out.printf("\t%d%n", b))
                .forEach(newPegs::flip);
            return newPegs;
        }
    }

    /**
     * Pairs of jump offsets and corresponding common neighbors offsets.
     */
    private static ImmutableMap<Position, Position> JUMPS = ImmutableMap.of(
        new Position(0, 2),    new Position(0, 1),
        new Position(2, 0),    new Position(1, 0),
        new Position(0, -2),   new Position(0, -1),
        new Position(-2, 0),   new Position(-1, 0)
    );


    private final int nrows;
    private final int ncols;
    private final ImmutableSet<Position> holes;
    private final ImmutableSet<Position> pegs;
    private final int solutionCount;
    private final PegEncoding encoding;
    private final BitSet rowMajorHoles;


    public PegsPuzzle(int nrows, int ncols, Set<Position> holes, Set<Position> pegs) {
        this(nrows, ncols, holes, pegs, 1); // default solution is to get to 1 peg
    }

    public PegsPuzzle(int nrows, int ncols, Set<Position> holes, Set<Position> pegs, int solutionCount) {
        if (!holes.stream().allMatch(hole -> hole.row() >= 0
                                          && hole.row() < nrows
                                          && hole.col() >= 0
                                          && hole.col() < ncols)) {
            throw new IllegalArgumentException(String.format("holes must all be in %d x %s grid", nrows, ncols));
        }
        if (!pegs.stream().allMatch(holes::contains)) {
            throw new IllegalArgumentException("all pegs must be in holes");
        }
        this.nrows = nrows;
        this.ncols = ncols;
        this.holes = ImmutableSet.copyOf(holes);
        this.pegs = ImmutableSet.copyOf(pegs);
        this.solutionCount = solutionCount;
        this.encoding = new PegEncoding(nrows, ncols);
        this.rowMajorHoles = encoding.toRowMajor(holes);
    }

    public int nrows() { return nrows; }
    public int ncols() { return ncols; }
    public ImmutableSet<Position> holes() { return holes; }
    public ImmutableSet<Position> pegs() { return pegs; }
    public int solutionCount() { return solutionCount; }


    public Optional<List<State>> solve(PuzzleSolver<State> solver) {
        return solver.solution(new State(pegs));
    }

    /**
     * Creates the initial state of a cross-shaped peg puzzle with the
     * initial empty hole in the center. The containing square must have
     * odd dimension, and the size of each arm must be odd and no greater
     * than the side of the containing square.
     * @param size number of holes in the center column (or center row)
     * @param armSize number of columns or rows in each arm of the cross
     * @throws IllegalArgumentException if {@code armSize > size}, if
     * either is not odd and positive
     */
    public static PegsPuzzle makeCross(int size, int armSize, int solutionCount) {
        int cornerSize = (size - armSize) / 2;
        int nPegs = (size * size) - (cornerSize * cornerSize * 4) - 1;

        //System.out.printf(
        //    "Making cross puzzle of size %d, arm size %d, corner size %d, and #pegs %d%n",
        //    size, armSize, cornerSize, nPegs);

        Position center = new Position(size/2, size/2);
        return new PegsPuzzle(size, size,
            crossHoles(size, armSize).toSet(),
            crossHoles(size, armSize).removeBy(c -> c, center).toSet(),
            solutionCount
        );
    }

    public static Funnel<State> stateFunnel() {
        return (from, into) -> into.putBytes(from.pegs.toByteArray());
    }

    static StreamEx<Position> crossHoles(int size, int armSize) {
        if (armSize > size || armSize < 1 || size < 1 || (armSize % 2) != 1 || (size % 2) != 1) {
            throw new IllegalArgumentException(
                "size arguments must be odd positive, arm size must be <= size");
        }
        int cornerSize = (size - armSize) / 2;
        int minArm = cornerSize;
        int maxArm = size - cornerSize - 1;
        return rowMajorPositions(size, size)
            .remove(c ->
                (c.row() < minArm || c.row() > maxArm) &&
                (c.col() < minArm || c.col() > maxArm)
            );
    }

    static StreamEx<Position> rowMajorPositions(int nrows, int ncols) {
        return IntStreamEx.range(0, nrows * ncols)
            .mapToEntry(i -> i / ncols, i -> i % ncols)
            .mapKeyValue((r, c) -> new Position(r, c));
    }

    public static void main(String[] args) {
        int size = Integer.parseInt(args[0]);
        int armSize = Integer.parseInt(args[1]);
        int remainder = size - armSize;
        int pegCount = size * size - remainder * remainder - 1;
        int solutionCount = 1;
        PegsPuzzle puzzle = makeCross(size, armSize, solutionCount);
        PuzzleSolver<State> solver = new DfsPuzzleSolver<>(
            //() -> new BloomPuzzleStateFilter<>(stateFunnel(), 100_000_000L, 0.0001)
        );
        System.out.printf("Solving %d x %d cross puzzle with arm %d, #pegs = %d, solution at %d%n",
            size, size, armSize, pegCount, solutionCount);
        Optional<List<State>> solution = puzzle.solve(solver);
        String result = solution.map(s -> StreamEx.of(s).joining("\n")).orElse("no solution");
        System.out.println(result);
    }
}
