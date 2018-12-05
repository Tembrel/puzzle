/**
 * A framework for describing and solving puzzles that can be characterized
 * as a problem of finding a sequence of transitions (or <em>moves</em>)
 * from an initial state to a solution state.
 * <p>
 * The primary abstraction is {@link net.peierls.puzzle.PuzzleState}, which defines a
 * successors function and test for whether a state represents a solution.
 * There is no separate puzzle type, because an initial puzzle state
 * instance essentially defines a puzzle: Find a sequence of states
 * starting with the initial state and ending with a solution state
 * with each element of the sequence other than first being a successor
 * of the previous element of the sequence.
 * <p>
 * To define a new kind of puzzle, implement the {@link net.peierls.puzzle.PuzzleState}
 * methods
 * {@link net.peierls.puzzle.PuzzleState#isSolution isSolution},
 * {@link net.peierls.puzzle.PuzzleState#successors successors},
 * and {@link net.peierls.puzzle.PuzzleState#predecessor predecessor},
 * and optionally any of the other methods.
 * <p>
 * The predecessor method should be defined
 * for states that are produced by {@code s0.successors()} to be
 * {@code s0}, otherwise it should be empty (initial states have no predecessor).
 * It is also acceptable to define predecessor as <strong>always</strong> empty,
 * in which case solution lists produced by {@link net.peierls.puzzle.FilteredPuzzleSolver}
 * will contain <strong>only</strong> the solution state, which might be acceptable
 * if the solution state itself contains all the relevant information.
 * <p>
 * States must be usable as map keys, so override {@code equals} and {@code hashCode}
 * properly.
 * Do <strong>not</strong> include a reference to a predecessor in the implementation
 * of these methods; a state might be reachable in two different ways, but we do not
 * in general want approximate containment tests to return false negatives from
 * two otherwise equal states comparing unequal due to different predecessors.
 * <p>
 * The {@link net.peierls.puzzle.PuzzleState#initialized initialized} method may be implemented
 * to avoid unnecessary computation on states that might have already been seen.
 * <p>
 * To define a new general-purpose solver, extend
 * {@link net.peierls.puzzle.FilteredPuzzleSolver} by
 * implementing {@link net.peierls.puzzle.FilteredPuzzleSolver#solutionState solutionState}.
 * Use {@link net.peierls.puzzle.FilteredPuzzleSolver#filterState filterState} before searching
 * from any state returned by the {@link net.peierls.puzzle.PuzzleState#successors state.successors()} method.
 * <p>
 * Two concrete implementations of {@link net.peierls.puzzle.FilteredPuzzleSolver} are
 * provided, {@link net.peierls.puzzle.DfsPuzzleSolver} and {@link net.peierls.puzzle.BfsPuzzleSolver},
 * correspondingly using depth-first and breadth-first search.
 * <p>
 * General-purpose solvers should allow users to provide a
 * {@link net.peierls.puzzle.PuzzleStateFilter} supplier.
 */
package net.peierls.puzzle;
