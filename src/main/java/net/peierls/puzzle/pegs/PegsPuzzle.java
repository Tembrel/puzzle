package net.peierls.puzzle.pegs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.hash.Funnel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.peierls.puzzle.BfsPuzzleSolver;
import net.peierls.puzzle.BloomPuzzleStateFilter;
import net.peierls.puzzle.PuzzleSolver;
import net.peierls.puzzle.PuzzleState;

import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;


/**
 * A peg-jumping puzzle.
 * @see https://www.chiark.greenend.org.uk/~sgtatham/puzzles/doc/pegs.html
 */
public final class PegsPuzzle {

    public static class Coord {
        private final int row;
        private final int col;

        Coord(int row, int col) { this.row = row; this.col = col; }

        public int row() { return row; }
        public int col() { return col; }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Coord)) return false;
            Coord that = (Coord) obj;
            return this.row == that.row && this.col == that.col;
        }
        @Override public int hashCode() {
            return Objects.hash(row, col);
        }
        @Override public String toString() {
            return String.format("(%d, %d)", row, col);
        }

        public Coord move(Coord amount) {
            return new Coord(row + amount.row, col + amount.col);
        }
    }

    static class Move {
        final Coord from;
        final Coord to;
        final Coord mid;
        Move(Coord from, Coord to, Coord mid) {
            this.from = from;
            this.to = to;
            this.mid = mid;
        }
        @Override public String toString() {
            return String.format(
                "Move peg at %s to %s, removing peg at %s",
                from, to, mid
            );
        }
    }

    public final class State implements PuzzleState<State> {
        private final State pred;
        private final Move move;
        private final ImmutableSet<Coord> pegs;


        State(Set<Coord> pegs) {
            this.pred = null;
            this.move = null;
            this.pegs = ImmutableSet.copyOf(pegs);
        }

        State(State pred, Move move) {
            this.pred = pred;
            this.move = move;
            this.pegs = pred.applyJump(move);
        }

        @Override public boolean isSolution() {
            return pegs.size() == 1;
        }

        @Override public Stream<State> successors() {
            //System.out.println("Searching from " + this);
            return StreamEx.of(pegs)
                .flatMap(this::moves)
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
            StringBuilder buf = new StringBuilder();
            if (move == null) {
                buf.append("\nInitial position:\n");
            } else {
                buf.append("\n").append(move).append("\n");
            }
            for (int row = 0; row < nrows; ++row) {
                for (int col = 0; col < ncols; ++col) {
                    Coord cur = new Coord(row, col);
                    if (pegs.contains(cur)) {
                        buf.append("X");
                    } else if (holes.contains(cur)) {
                        buf.append(".");
                    } else {
                        buf.append(" ");
                    }
                }
                buf.append("\n");
            }
            return buf.toString();
        }

        public Coord dimensions() {
            return new Coord(nrows, ncols);
        }

        public Set<Coord> holes() {
            return holes;
        }

        private StreamEx<Move> moves(Coord from) {
            return EntryStream.of(JUMPS)
                .mapKeys(from::move)            // keys are jump targets
                .filterKeys(holes::contains)    // jumps must be to holes
                .removeKeys(pegs::contains)     // jumps must be to empty holes
                .mapValues(from::move)          // values are intervening holes
                .filterValues(pegs::contains)   // can only jump if peg intervening
                .mapKeyValue((to, mid) -> new Move(from, to, mid));
        }

        private ImmutableSet<Coord> applyJump(Move move) {
            return StreamEx.of(pegs)
                .remove(peg -> peg.equals(move.from))
                .remove(peg -> peg.equals(move.mid))
                .append(move.to)
                .collect(toImmutableSet());
        }
    }

    /**
     * Pairs of jump offsets and corresponding common neighbors offsets.
     */
    private static ImmutableMap<Coord, Coord> JUMPS = ImmutableMap.of(
        new Coord(0, 2),    new Coord(0, 1),
        new Coord(2, 0),    new Coord(1, 0),
        new Coord(0, -2),   new Coord(0, -1),
        new Coord(-2, 0),   new Coord(-1, 0)
    );


    private final int nrows;
    private final int ncols;
    private final ImmutableSet<Coord> holes;
    private final ImmutableSet<Coord> pegs;


    public PegsPuzzle(int nrows, int ncols, Set<Coord> holes, Set<Coord> pegs) {
        if (!pegs.stream().allMatch(holes::contains)) {
            throw new IllegalArgumentException("all pegs must be in holes");
        }
        this.nrows = nrows;
        this.ncols = ncols;
        this.holes = ImmutableSet.copyOf(holes);
        this.pegs = ImmutableSet.copyOf(pegs);
    }


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
    public static PegsPuzzle makeCross(int size, int armSize) {
        if (armSize > size || armSize < 1 || size < 1 || (armSize % 2) != 1 || (size % 2) != 1) {
            throw new IllegalArgumentException(
                "size arguments must be odd positive, arm size must be <= size");
        }
        int mid = size / 2; // integer divide truncates
        int cornerSize = (size - armSize) / 2;
        int minArm = cornerSize;
        int maxArm = size - cornerSize - 1;
        ImmutableSet.Builder<Coord> holes = ImmutableSet.builder();
        ImmutableSet.Builder<Coord> pegs = ImmutableSet.builder();
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                if (row < minArm && (col < minArm || col > maxArm)) {
                    continue;
                }
                if (row > maxArm && (col < minArm || col > maxArm)) {
                    continue;
                }
                Coord cur = new Coord(row, col);
                holes.add(cur);
                if (row == mid && col == mid) {
                    continue;
                }
                pegs.add(cur);
            }
        }
        return new PegsPuzzle(size, size, holes.build(), pegs.build());
    }

    public static Funnel<State> stateFunnel() {
        return (from, into) -> {
            into.putInt(from.pegs.size());
            from.pegs.stream().forEach(peg -> {
                into.putInt(peg.row);
                into.putInt(peg.col);
            });
        };
    }

    public static void main(String[] args) {
        int size = Integer.parseInt(args[0]);
        int armSize = Integer.parseInt(args[1]);
        int cornerSize = (size - armSize) / 2;
        int nPegs = (size * size) - (cornerSize * cornerSize * 4) - 1;
        System.out.printf("Solving cross puzzle of size %d, arm size %d, corner size %d, and #pegs %d%n", size, armSize, cornerSize, nPegs);
        PegsPuzzle puzzle = makeCross(size, armSize);
        PuzzleSolver<State> solver = new BfsPuzzleSolver<>(() ->
            new BloomPuzzleStateFilter<>(stateFunnel(), 8_000_000L, 0.0001));
        Optional<List<State>> solution = puzzle.solve(solver);
        String result = solution.map(s -> StreamEx.of(s).joining("\n")).orElse("no solution");
        System.out.println(result);
    }
}
