package net.peierls.puzzle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import java.util.function.Supplier;

import one.util.streamex.StreamEx;


/**
 * Breadth-first-search puzzle solver.
 */
public class BfsPuzzleSolver<T extends PuzzleState<T>> extends CachedPuzzleSolver<T> {

    /**
     * Constructs a BFS solver with an exact (non-lossy) cache.
     */
    public BfsPuzzleSolver() {
        super();
    }

    /**
     * Constructs a BFS solver that will use a BloomFilter cache for
     * states that support it, otherwise an exact (non-lossy) cache.
     */
    public BfsPuzzleSolver(Supplier<PuzzleStateCache<T>> cacheSupplier) {
        super(cacheSupplier);
    }


    @Override
    protected Optional<T> solutionState(T initialState, PuzzleStateCache<T> cache) {
        Deque<T> queue = new ArrayDeque<>();
        try {
            return bfs(initialState, cache, queue)
                //.peek(this::trace)
                .findAny(PuzzleState::isSolution);
        } finally {
            System.out.printf("BFS queue size: %d%n", queue.size());
        }
    }

    private StreamEx<T> bfs(T initialState, PuzzleStateCache<T> cache, Deque<T> queue) {
        queue.offerLast(initialState);
        return StreamEx.produce(action -> {
            T state = queue.pollFirst();
            if (state == null) {
                return false;
            }
            state = filterState(state, cache);
            if (state != null) {
                action.accept(state);
                state.successors().forEach(queue::offerLast);
            }
            return true;
        });
    }
}
