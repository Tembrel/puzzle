package net.peierls.puzzle;

import com.google.common.hash.Funnel;

import java.util.Optional;
import java.util.stream.*;

import one.util.streamex.*;


/**
 * A state that a puzzle of parameterized type T can be in.
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
     * from this state.
     */
    boolean isHopeless();

    /**
     * The stream of puzzle states obtainable by
     * a single move from this state.
     */
    Stream<T> successors();


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
     */
    default int score() {
        return 0;
    }


    /**
     * Optionally returns a funnel for encoding arbitrary instances
     * of this type, for use in approximate containment tests.
     */
    default Optional<Funnel<T>> funnel() {
        return Optional.empty();
    }
}
