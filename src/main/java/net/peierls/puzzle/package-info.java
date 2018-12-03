/**
 * A framework for describing and solving puzzles that involve moves
 * from an initial state to a solution state.
 * <p>
 * There is no separate puzzle type, because an initial puzzle state
 * instance essentially defines a puzzle: Find a sequence of states
 * starting with the initial state and ending with a solution state
 * with each element of the sequence other than first being a successor
 * of the previous element of the sequence.
 * <p>
 * To define a new kind of puzzle, implement {@link PuzzleState}'s
 * {@link #isSolution}, {@link #successors), and {@link #predecessor}
 * methods, and optionally any of the other methods. The predecessor
 * method should be defined for states that are produced by
 * {@code s0.successors()} to be {@code s0}, otherwise it should be empty
 * (initial states have no predecessor).
 * <p>
 * To define a new general-purpose solver, extend {@link FilteredPuzzleSolver} by
 * implementing {@link FilteredPuzzleSolver#solutionState solutionState(initialState, filter)}.
 * Use {@link FilteredPuzzleSolver#filterState filterState(state, filter)} before searching
 * from any state returned by a call to the {@link PuzzleState#successors} method.
 * (The initial state is added to the filter and checked for hopelessness automatically.)
 * <p>
 */
package net.peierls.puzzle;
