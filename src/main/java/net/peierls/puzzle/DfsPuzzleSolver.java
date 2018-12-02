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
    Optional<T> solutionState(T initialState, PuzzleStateFilter<T> filter) {
        return solutionState(initialState, filter, 0);
    }

    Optional<T> solutionState(T state, PuzzleStateFilter<T> filter, int depth) {
        if (state.isSolution()) {
            return Optional.of(state);
        } else if (!state.isHopeless() && depth < MAX_DEPTH) {
            for (T next : StreamEx.of(state.successors())) {
                Optional<T> solved = solutionState(next, filter, depth + 1);
                if (solved.isPresent()) {
                    return solved;
                }
            }
        }
        return Optional.empty();
    }
}
