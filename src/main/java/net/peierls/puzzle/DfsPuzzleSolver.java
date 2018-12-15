package net.peierls.puzzle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import java.util.function.Supplier;

import one.util.streamex.StreamEx;


/**
 * Depth-first-search puzzle solver. Warning: This doesn't work very well unless
 * the puzzle state can recognize hopelessness before the stack overflows.
 */
public class DfsPuzzleSolver<T extends PuzzleState<T>> extends CachedPuzzleSolver<T> {


    /**
     * Constructs a DFS solver with an exact (non-lossy) cache and the given max depth.
     */
    public DfsPuzzleSolver() {
        super();
    }

    /**
     * Constructs a DFS solver with the given max depth that will use
     * a BloomFilter with the given parameters, otherwise an exact cache.
     */
    public DfsPuzzleSolver(Supplier<PuzzleStateCache<T>> cacheSupplier) {
        super(cacheSupplier);
    }


    @Override
    protected Optional<T> solutionState(T initialState, PuzzleStateCache<T> cache) {
        Deque<T> stack = new ArrayDeque<>();
        try {
            return dfs(initialState, cache, stack)
                //.peek(this::trace)
                .findAny(PuzzleState::isSolution);
        } finally {
            System.out.printf("DFS stack size: %d%n", stack.size());
        }
    }

    private StreamEx<T> dfs(T initialState, PuzzleStateCache<T> cache, Deque<T> stack) {
        stack.offerFirst(initialState);
        return StreamEx.produce(action -> {
            T state = stack.pollFirst();
            if (state == null) {
                return false;
            }
            state = filterState(state, cache);
            if (state != null) {
                action.accept(state);
                state.successors().forEach(stack::offerFirst);
            }
            return true;
        });
    }
}
