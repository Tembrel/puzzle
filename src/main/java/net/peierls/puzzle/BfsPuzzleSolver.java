package net.peierls.puzzle;

import java.util.Optional;

import one.util.streamex.StreamEx;


/**
 * Breadth-first-search puzzle solver.
 */
public class BfsPuzzleSolver<T extends PuzzleState<T>> extends AbstractPuzzleSolver<T> {

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
    Optional<T> solutionState(T initialState, PuzzleStateFilter<T> filter) {
        return bfs(StreamEx.of(initialState), filter)
            //.peek(this::trace)
            .findAny(PuzzleState::isSolution);
    }

    StreamEx<T> bfs(StreamEx<T> states, PuzzleStateFilter<T> filter) {
        return states.headTail((state, rest) ->
            bfs(maybeAddSuccessors(state, rest, filter), filter)
                .prepend(state));
    }

    StreamEx<T> maybeAddSuccessors(T state, StreamEx<T> rest, PuzzleStateFilter<T> filter) {
        if (filter.put(state) && !state.isHopeless()) {
            // We haven't seen this state before and it isn't hopeless to search
            // further from it, so we append its successors to whatever is left
            // in the queue.
            return rest.append(state.successors());
        } else {
            // Either we've seen this state before or it's hopeless to search
            // its successors, so we just return whatever's left in the queue.
            return rest;
        }
    }

    private void trace(T state) {
        System.out.printf("searching %s, pred %s%n",
            state,
            state.predecessor().map(Object::toString).orElse("-"));
    }
}
