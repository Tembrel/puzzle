package net.peierls.puzzle;

import java.util.List;
import java.util.Optional;


/**
 * Interface for algorithms that search for solutions to a puzzles
 * of the parameterized type.
 */
public interface PuzzleSolver<T extends PuzzleState<T>> {

    /**
     * Finds a solution to puzzle defined by initial state.
     *
     * @return the solution to the puzzle as a list of states from
     * the initial state to a solved state (inclusive), or an empty
     * optional if solution is unreachable from initial state.
     */
    default Optional<List<T>> solution(T initialState) {
        return solution(initialState, new ExactPuzzleStateFilter<>());
    }


    /**
     * Finds a solution to puzzle defined by initial state using
     * the given state filter.
     *
     * @return the solution to the puzzle as a list of states from
     * the initial state to a solved state (inclusive), or an empty
     * optional if solution is unreachable from initial state under
     * the limitations of the given state filter.
     */
    Optional<List<T>> solution(T initialState, PuzzleStateFilter<T> filter);
}
