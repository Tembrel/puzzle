package net.peierls.puzzle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import one.util.streamex.StreamEx;


/**
 * Breadth-first-search puzzle solver.
 */
public class BfsPuzzleSolver<T extends PuzzleState<T>> extends FilteredPuzzleSolver<T> {

    /**
     * Constructs a BFS solver with an exact filter.
     */
    public BfsPuzzleSolver() {
        super();
    }

    /**
     * Constructs a BFS solver that will use a Bloom filter for states
     * that support it, otherwise an exact filter.
     */
    public BfsPuzzleSolver(int expectedInsertions, double fpp) {
        super(expectedInsertions, fpp);
    }


    @Override
    protected Optional<T> solutionState(T initialState, PuzzleStateFilter<T> filter) {
        return bfs(initialState, filter)
            .parallel()
            //.peek(this::trace)
            .findAny(PuzzleState::isSolution);
    }

    private StreamEx<T> bfs(T initialState, PuzzleStateFilter<T> filter) {
        Deque<T> queue = new ArrayDeque<>();
        queue.offer(initialState);
        return StreamEx.produce(action -> {
            T state = queue.poll();
            if (state == null) {
                return false;
            }
            state = filterState(state, filter);
            if (state != null) {
                action.accept(state);
                state.successors().forEach(queue::offer);
            }
            return true;
        });
    }

    private void trace(T state) {
        System.out.printf("searching %s, pred %s%n",
            state,
            state.predecessor().map(Object::toString).orElse("-"));
    }
}
