package net.peierls.puzzle;

import java.util.Optional;

import one.util.streamex.StreamEx;


/**
 * Depth-first-search puzzle solver. Warning: This doesn't work very well unless
 * the puzzle state can recognize hopelessness before the stack overflows.
 */
public class DfsPuzzleSolver<T extends PuzzleState<T>> extends AbstractPuzzleSolver<T> {

    final int MAX_DEPTH = 20;

    /**
     * Constructs a DFS solver with an exact filter.
     */
    public DfsPuzzleSolver() {
        super();
    }

    /**
     * Constructs a DFS solver that will use a Bloom filter for states
     * that support it, otherwise an exact filter.
     */
    public DfsPuzzleSolver(int expectedInsertions, double fpp) {
        super(expectedInsertions, fpp);
    }


    @Override
    protected Optional<T> solutionState(T initialState, PuzzleStateFilter<T> filter) {
        if (initialState == null || filter == null) {
            throw new NullPointerException();
        }
        return Optional.ofNullable(solutionState(initialState, filter, 0));
    }

    T solutionState(T state, PuzzleStateFilter<T> filter, int depth) {
        state = usableState(state, filter);
        if (state == null) {
            return null;
        } else if (state.isSolution()) {
            return state;
        } else if (depth < MAX_DEPTH) {
            return StreamEx.of(state.successors())
                .map(next -> solutionState(next, filter, depth + 1))
                .nonNull()
                .findAny()
                .orElse(null);
        } else {
            return null;
        }
    }
}
