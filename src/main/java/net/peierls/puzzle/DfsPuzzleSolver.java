package net.peierls.puzzle;

import java.util.Optional;

import one.util.streamex.StreamEx;


/**
 * Depth-first-search puzzle solver. Warning: This doesn't work very well unless
 * the puzzle state can recognize hopelessness before the stack overflows.
 */
public class DfsPuzzleSolver<T extends PuzzleState<T>> extends AbstractPuzzleSolver<T> {

    private static final int MAX_DEPTH = 20;


    private final int maxDepth;


    /**
     * Constructs a DFS solver with an exact filter and the default max depth.
     */
    public DfsPuzzleSolver() {
        this(MAX_DEPTH);
    }

    /**
     * Constructs a DFS solver with an exact filter and the given max depth.
     */
    public DfsPuzzleSolver(int maxDepth) {
        super();
        this.maxDepth = maxDepth;
    }

    /**
     * Constructs a DFS solver with the default max depth that will use
     * a Bloom filter for states that support it, otherwise an exact filter.
     */
    public DfsPuzzleSolver(int expectedInsertions, double fpp) {
        this(MAX_DEPTH, expectedInsertions, fpp);
    }

    /**
     * Constructs a DFS solver with the given max depth that will use
     * a Bloom filter for states that support it, otherwise an exact filter.
     */
    public DfsPuzzleSolver(int maxDepth, int expectedInsertions, double fpp) {
        super(expectedInsertions, fpp);
        this.maxDepth = maxDepth;
    }


    @Override
    protected Optional<T> solutionState(T initialState, PuzzleStateFilter<T> filter) {
        if (initialState == null || filter == null) {
            throw new NullPointerException("arguments must be non-null");
        }
        return dfs(initialState, filter, 0)
            .findAny(PuzzleState::isSolution);
    }

    StreamEx<T> dfs(T state, PuzzleStateFilter<T> filter, int depth) {
        state = searchableState(state, filter);
        if (state != null && depth < maxDepth) {
            return StreamEx.of(state.successors())
                .parallel()
                .flatMap(next -> dfs(next, filter, depth + 1))
                .prepend(state);
        } else {
            return StreamEx.empty();
        }
    }
}
