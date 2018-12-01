package net.peierls.puzzle;

import com.google.common.hash.Funnel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * Abstract base for puzzle solvers that use a {@link PuzzleStateFilter}
 * to keep track (possibility only approximately) of which states have
 * been seen during the search for a solution. General-purpose solvers
 * necessarily fall into this category, because otherwise there is no
 * way to know in general whether a particular state has already been
 * examined. (Custom solvers for specific puzzle state types might be
 * able to get around this with domain-specific information.)
 * <p>
 * Because in general the search space can be so large that it is not
 * possible to store all states that have been seen (even taking advantage
 * of compressed forms), the {@link PuzzleStateFilter} abstraction is
 * used to allow for searches that use much less memory but that may fail
 * occasionally to search a valid branch.
 */
public abstract class AbstractPuzzleSolver<T extends PuzzleState<T>> implements PuzzleSolver<T> {

    private final Function<Funnel<T>, PuzzleStateFilter<T>> funnelToFilter;


    /**
     * Constructs a solver that will always use an exact filter, which
     * will store each state seen but will always report correctly on
     * whether a state has been seen.
     */
    AbstractPuzzleSolver() {
        this.funnelToFilter = funnel -> exactFilter();
    }

    /**
     * Constructs a solver that will always use the given state filter.
     */
    AbstractPuzzleSolver(PuzzleStateFilter<T> filter) {
        this.funnelToFilter = funnel -> filter;
    }

    /**
     * Constructs a solver that will use an exact filter when solving
     * for initial states that do not define a funnel, and a BloomFilter
     * when solving initial states that do define a funnel. The two
     * parameters to this method will be ignored in the former case.
     */
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
