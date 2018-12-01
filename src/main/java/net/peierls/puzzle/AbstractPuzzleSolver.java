package net.peierls.puzzle;

import com.google.common.hash.Funnel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * Abstract base for puzzle solvers that use a puzzle state filter to
 * keep track (possibility only approximately) of which states have
 * been seen during the search for a solution.
 */
public abstract class AbstractPuzzleSolver<T extends PuzzleState<T>> implements PuzzleSolver<T> {

    private final Function<Funnel<T>, PuzzleStateFilter<T>> funnelToFilter;


    AbstractPuzzleSolver() {
        this.funnelToFilter = funnel -> exactFilter();
    }

    AbstractPuzzleSolver(PuzzleStateFilter<T> filter) {
        this.funnelToFilter = funnel -> filter;
    }

    AbstractPuzzleSolver(int expectedInsertions, double fpp) {
        this.funnelToFilter = funnel -> bloomFilter(funnel, expectedInsertions, fpp);
    }


    @Override
    public final Optional<List<T>> solution(T initialState) {
        PuzzleStateFilter<T> filter = initialState.funnel()
            .map(funnel -> funnelToFilter.apply(funnel))
            .orElseGet(this::exactFilter);
        return solution(initialState, filter);
    }


    /**
     * Concrete subclasses must implement this method to
     * find a solution from the given initial state, using
     * the given state filter.
     */
    abstract Optional<List<T>> solution(T initialState, PuzzleStateFilter<T> filter);


    private PuzzleStateFilter<T> bloomFilter(Funnel<T> funnel, int expectedInsertions, double fpp) {
        return new BloomPuzzleStateFilter<>(funnel, expectedInsertions, fpp);
    }

    private PuzzleStateFilter<T> exactFilter() {
        return new ExactPuzzleStateFilter<>();
    }
}
