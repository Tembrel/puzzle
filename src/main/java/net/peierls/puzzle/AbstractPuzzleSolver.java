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

    private volatile long lastApproximateElementCount = 0L;
    private volatile double lastExpectedFpp = 0f;


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
        try {
            Optional<T> finalState = solutionState(initialState, filter);
            return finalState.map(PuzzleState::toSolution);
        } finally {
            lastApproximateElementCount = filter.approximateElementCount();
            lastExpectedFpp = filter.expectedFalsePositiveProbability();
        }
    }


    /**
     * Concrete subclasses must implement this method to
     * find a solution from the given initial state, using
     * the given state filter.
     */
    protected abstract Optional<T> solutionState(T initialState, PuzzleStateFilter<T> filter);


    /**
     * If state is not null or hopeless and has definitely not been
     * seen by the filter, returns an precomputed copy of state
     * (and as a side-effect marks state as seen in the filter),
     * otherwise returns null. The test for hopelessness is made
     * on the precomputed copy.
     */
    protected T searchableState(T state, PuzzleStateFilter<T> filter) {
        if (state == null || !filter.put(state)) {
            return null;
        }

        // First time seeing state, so precompute it.
        state = state.precomputed();

        if (state.isHopeless()) {
            return null;
        } else {
            return state;
        }
    }

    long lastApproximateElementCount() {
        return lastApproximateElementCount;
    }

    double lastExpectedFpp() {
        return lastExpectedFpp;
    }

    private PuzzleStateFilter<T> bloomFilter(Funnel<T> funnel, int expectedInsertions, double fpp) {
        return new BloomPuzzleStateFilter<>(funnel, expectedInsertions, fpp);
    }

    private PuzzleStateFilter<T> exactFilter() {
        return new ExactPuzzleStateFilter<>();
    }
}
