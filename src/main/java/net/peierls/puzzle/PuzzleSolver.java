package net.peierls.puzzle;

import java.util.List;
import java.util.Optional;


/**
 * Searches for a solution to a puzzle of the parameterized type.
 */
public interface PuzzleSolver<T extends PuzzleState<T>> {

    /**
     * Finds a solution to puzzle defined by initial state.
     *
     * @return the solution to the puzzle as a list of states from
     * the initial state to a solved state (inclusive), or an empty
     * optional if solution is unreachable from initial state.
     */
    Optional<List<T>> solution(T initialState);
}
