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
 * To define a new kind of puzzle, implement {@link PuzzleState PuzzleState}'s
 * {@link PuzzleState#isSolution isSolution},
 * {@link PuzzleState#successors successors},
 * and {@link PuzzleState#predecessor predecessor} methods, and optionally any
 * of the other methods. The predecessor method should be defined
 * for states that are produced by {@code s0.successors()} to be
 * {@code s0}, otherwise it should be empty (initial states have no predecessor).
 * <p>
 * The {@link PuzzleState#precomputed precomputed} method may be implemented
 * to avoid unnecessary computation on states that might have already been seen.
 * The {@link PuzzleState#funnel} method can be implemented to provide a compact
 * encoding of a state for use in conjunction with an approximate containment filter.
 * <p>
 * To define a new general-purpose solver, extend
 * {@link FilteredPuzzleSolver} by
 * implementing {@link FilteredPuzzleSolver#solutionState solutionState}.
 * Use {@link FilteredPuzzleSolver#filterState filterState} before searching
 * from any state returned by the {@link PuzzleState#successors state.successors()} method.
 * <p>
 */
package net.peierls.puzzle;
