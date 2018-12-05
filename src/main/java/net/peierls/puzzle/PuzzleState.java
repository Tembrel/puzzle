package net.peierls.puzzle;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * A representation of the state of a puzzle that involves
 * finding a sequence of moves from an initial state to
 * a solution state.
 * <ul>
 * <li>
 * A state can stream its successors, which are the states
 * resulting in a single move from it.
 * </li><li>
 * A puzzle state can report whether it is a solution state or
 * whether it is "hopeless", meaning that no sequence of
 * moves from it will result in a solution state.
 * </li><li>
 * A puzzle state knows its predecessor state, and this
 * information is not required to be used in defining state
 * equivalence, i.e., two states with different predecessors are
 * permitted to compare equal.
 * </li><li>
 * A puzzle state may be initialized with precomputed values through the
 * {@link #initialized} method. This allows solvers to avoid unnecessary
 * expensive computation on states that have already been seen.
 * It is permissible to return self from this method if precautions
 * for mutability under concurrent access are taken, e.g., if the state
 * type is immutable and no precomputation is needed.
 * </li><li>
 * A puzzle state can produce a solution, i.e., a list of states from some
 * initial state to the current state, where each element other than the
 * first is a successor to the previous state in the list. The current state
 * is not required to be a solution state.
 * </li>
 * </ul>
 */
public interface PuzzleState<T extends PuzzleState<T>> {

    /**
     * Returns whether this is a solution state.
     */
    boolean isSolution();

    /**
     * Returns true if it is known that no solution
     * state can be reached from this state.
     * Returns false otherwise, i.e., if it is not known
     * that no solution state can be reached from this state,
     * or if it is known that a solution state can be reached
     * from this state. The default implementation returns false.
     */
    default boolean isHopeless() {
        return false;
    }

    /**
     * The stream of puzzle states obtainable by
     * a single move from this state.
     */
    Stream<T> successors();


    /**
     * The state from which this state was reached,
     * or empty if this is an initial state.
     */
    Optional<T> predecessor();


    /**
     * Returns a state that is equivalent to this state,
     * but which might implement some methods more efficiently,
     * e.g., by precomputing commonly needed values.
     * The return value can be this instance itself, and that is
     * how the method is implemented by default.
     */
    @SuppressWarnings("unchecked")
    default T initialized() {
        return (T) this;
    }


    /**
     * A rating of how good this state is. Lower is better. You can
     * return the same value for all states if you don't know.
     * Puzzle solvers are not required to pay attention to scores
     * unless they are looking for a best solution. The default
     * implementation returns zero.
     */
    default int score() {
        return 0;
    }
}
