package net.peierls.puzzle;

import java.util.List;
import java.util.Optional;


/**
 * Interface for algorithms that search for solutions to a puzzles
 * of the parameterized type.
 */
public abstract class AbstractPuzzleSolver<T extends PuzzleState<T>>
        implements PuzzleSolver<T>, PuzzleStateFilter<T> {

    private volatile PuzzleStateFilter<T> filter;

    AbstractPuzzleSolver() {
        this.filter = null;
    }

    AbstractPuzzleSolver(PuzzleStateFilter<T> filter) {
        this.filter = filter;
    }

    @Override
    public boolean mightContain(T state) {
        return filter.mightContain(state);
    }

    @Override
    public boolean put(T state) {
        return filter.put(state);
    }

    @Override
    public long approximateElementCount() {
        return filter.approximateElementCount();
    }

    @Override
    public double expectedFalsePositiveProbability() {
        return filter.expectedFalsePositiveProbability();
    }

    @Override
    public final Optional<List<T>> solution(T initialState) {
        synchronized (this) {
            if (filter == null) {
                filter = initialState.newFilterInstance();
            }
        }
        return solve(initialState);
    }

    /**
     * Concrete subclasses must implement this method to
     * find a solution from the given initial state,
     * presumably using the puzzle state filter methods
     * to limit the search, though this is not enforced.
     */
    abstract Optional<List<T>> solve(T initialState);
}
