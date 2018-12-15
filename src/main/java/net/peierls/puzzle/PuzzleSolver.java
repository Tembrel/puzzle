package net.peierls.puzzle;

import java.util.List;


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
     * list if this solve cannot reach a solution from the initial state.
     */
    List<T> solution(T initialState);
}
