package net.peierls.puzzle;

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
}
