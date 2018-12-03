package net.peierls.puzzle.client;

import com.google.common.collect.*;
import java.util.*;
import static java.util.stream.Collectors.counting;

/**
 * A puzzle on a graph of three interlocking circles with 12 vertices
 * colored either red or green (6 each). Each circle can be rotated in either
 * direction, and the goal is to move all the green vertices into the triangle
 * whose vertices are the three outer vertices of the puzzle that are shared
 * by two circles.
 * <pre>
 *    0   1   2
 *  3   4   5   6
 *    7   8   9
 *     10  11
 * </pre>
 * This implementation uses breadth-first search to find a solution.
 */
public class CirclePuzzle {

    enum Color { RED, GREEN }

    /**
     * Initial position, assigning red and green to the vertices
     * of the puzzle.
     * <pre>
     *    R   G   G
     *  R   G   R   R
     *    G   R   R
     *      G   G
     * </pre>
     */
    static final Position INITIAL_POSITION = Position.of(
        Color.RED, Color.GREEN, Color.GREEN,
        Color.RED, Color.GREEN, Color.RED, Color.RED,
        Color.GREEN, Color.RED, Color.RED,
        Color.GREEN, Color.GREEN
    );

    /** Attempt to solve a circle puzzle from the given initial position. */
    public static void main(String args[]) {
        CirclePuzzle puzzle = new CirclePuzzle(INITIAL_POSITION);
        Optional<Position> solution = puzzle.solve();
        if (solution.isPresent()) {
            System.out.println("Solved: " + solution.get());
        } else {
            System.out.println("Not solved");
        }
    }


    final Queue<Position> queue;
    final Set<ImmutableList<Color>> seen;

    CirclePuzzle(Position initialPosition) {
        this.queue = new ArrayDeque<>();
        this.seen = new HashSet<>();
        queue.add(initialPosition);
    }

    /**
     * Breadth-first search using a queue and a set of already-seen positions.
     */
    Optional<Position> solve() {
        for (Position position; (position = queue.poll()) != null; ) {
            if (position.isSolution()) {
                return Optional.of(position);
            }
            if (!seen.contains(position.coloring)) {
                seen.add(position.coloring);
                Rotation.ALL_ROTATIONS.stream()
                    .map(position::rotate)
                    .forEach(newPosition -> queue.add(newPosition));
            }
        }
        return Optional.empty();
    }


    /*
     * A rotation of one of the three interlocking circles of the puzzle
     * graph by one step, either clockwise (CW) or counter-clockwise (CCW).
     */
    static class Rotation {
        /** Direction of rotation */
        enum Direction { CW, CCW }

        /** Indices of circle A (center at 4) */
        static final ImmutableList<Integer> CIRCLE_A = ImmutableList.of(0, 1, 5, 8, 7, 3);

        /** Indices of circle B (center at 5) */
        static final ImmutableList<Integer> CIRCLE_B = ImmutableList.of(1, 2, 6, 9, 8, 4);

        /** Indices of circle C (center at 8) */
        static final ImmutableList<Integer> CIRCLE_C = ImmutableList.of(4, 5, 9, 11, 10, 7);


        final String circleId;
        final ImmutableList<Integer> circle;
        final Direction direction;

        Rotation(String circleId, ImmutableList<Integer> circle, Direction direction) {
            this.circleId = circleId;
            this.circle = circle;
            this.direction = direction;
        }

        ImmutableList<Integer> indices() {
            return direction == Direction.CW ? circle : circle.reverse();
        }

        @Override public String toString() {
            return String.format("%s-%s", circleId, direction);
        }

        /**
         * The possible rotations, explicitly enumerated: three circles (A, B, C),
         * two directions (CW, CCW).
         */
        static final ImmutableSet<Rotation> ALL_ROTATIONS = ImmutableSet.of(
            new Rotation("A", CIRCLE_A, Direction.CW),
            new Rotation("A", CIRCLE_A, Direction.CCW),
            new Rotation("B", CIRCLE_B, Direction.CW),
            new Rotation("B", CIRCLE_B, Direction.CCW),
            new Rotation("C", CIRCLE_C, Direction.CW),
            new Rotation("C", CIRCLE_C, Direction.CCW)
        );
    }


    /**
     * A reachable position of the puzzle, with a vertex coloring,
     * the previous position, and the rotation from the previous
     * position into this one.
     */
    static class Position {
        /** The indices of the triangle that must all be green for the goal position. */
        static final ImmutableList<Integer> TRIANGLE = ImmutableList.of(1, 4, 5, 7, 8, 9);

        final ImmutableList<Color> coloring;
        final Position parent;
        final Rotation rotation;

        Position(List<Color> coloring, Position parent, Rotation rotation) {
            this.coloring = ImmutableList.copyOf(coloring);
            this.parent = parent;
            this.rotation = rotation;
        }

        /** Create an initial position from an array of 12 colors. */
        static Position of(Color... colors) {
            if (colors.length != 12) {
                throw new IllegalArgumentException("Supply exactly 12 colors");
            }
            List<Color> coloring = ImmutableList.copyOf(colors);
            if (coloring.stream().filter(c -> c == Color.GREEN).collect(counting()) != 6) {
                throw new IllegalArgumentException("Supply exactly 6 greens");
            }
            return new Position(coloring, null, null);
        }

        /** Creates a new position from this one under the given rotation. */
        Position rotate(Rotation rotation) {
            Color[] colors = coloring.toArray(new Color[coloring.size()]);
            ImmutableList<Integer> indices = rotation.indices();
            int n = indices.size();
            for (int i = 0; i < n; ++i) {
                int j = (i+1) % n;
                colors[indices.get(j)] = coloring.get(indices.get(i));
            }
            return new Position(ImmutableList.copyOf(colors), this, rotation);
        }

        /** True if all vertices indexed by TRIANGLE are green. */
        boolean isSolution() {
            return TRIANGLE.stream()
                .mapToInt(i -> i)
                .allMatch(i -> coloring.get(i) == Color.GREEN);
        }

        /** Chain back through the parent links to collect rotations in reverse order. */
        Deque<Rotation> rotations() {
            Deque<Rotation> rotations = new ArrayDeque<>();
            Position position = this;
            while (position.rotation != null) {
                rotations.addFirst(position.rotation);
                position = position.parent;
            }
            return rotations;
        }

        @Override public String toString() {
            return rotations().toString();
        }
    }
}
