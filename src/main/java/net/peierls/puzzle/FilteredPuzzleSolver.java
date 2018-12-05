package net.peierls.puzzle;

import com.google.common.collect.ImmutableList;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import one.util.streamex.StreamEx;


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
 * <p>
 * The {@link #solution} method is final; subclasses should implement
 * {@link #solutionState solutionState(initialState, filter)} and use the
 * {@link #filterState filterState(state, filter)} method before searching a state.
 */
public abstract class FilteredPuzzleSolver<T extends PuzzleState<T>> implements PuzzleSolver<T> {

    private final Supplier<PuzzleStateFilter<T>> filterSupplier;

    private volatile long lastApproximateElementCount = 0L;
    private volatile double lastExpectedFpp = 0f;


    /**
     * Constructs a solver that will always use an exact filter, which
     * will store each state seen but will always report correctly on
     * whether a state has been seen.
     */
    protected FilteredPuzzleSolver() {
        this.filterSupplier = ExactPuzzleStateFilter::new;
    }

    /**
     * Constructs a solver that will always use the given state filter.
     */
    protected FilteredPuzzleSolver(Supplier<PuzzleStateFilter<T>> filterSupplier) {
        this.filterSupplier = filterSupplier;
    }


    @Override
    public final Optional<List<T>> solution(T initialState) {
        if (initialState == null) {
            throw new NullPointerException("initial state must not be null");
        }
        PuzzleStateFilter<T> filter = filterSupplier.get();
        try {
            Optional<T> finalState = solutionState(initialState, filter);
            return finalState.map(this::toSolution);
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
     * If state is neither null nor hopeless and definitely hasn't
     * been seen by the filter, returns an initialized copy of state
     * (and as a side-effect marks state as seen in the filter),
     * otherwise returns null. The test for hopelessness is made
     * on the initialized copy.
     */
    protected T filterState(T state, PuzzleStateFilter<T> filter) {
        if (state == null || !filter.put(state)) {
            // Null state or state might not have been seen.
            return null;
        }

        // First time seeing state, so precompute it.
        state = state.initialized();

        if (state.isHopeless()) {
            return null;
        } else {
            return state;
        }
    }

    /**
     * Returns the successors of state, filtering out already-seen
     * or hopeless states, and marking these successors as seen and
     * converting them to their initialized versions. This is
     * an alternative to using separate calls to {@link PuzzleState#successors}
     * and {@link #filterState filterState}.
     */
    protected Stream<T> successors(T state, PuzzleStateFilter<T> filter) {
        return state.successors()
            .filter(s -> filter.put(s));
    }

    long lastApproximateElementCount() {
        return lastApproximateElementCount;
    }

    double lastExpectedFpp() {
        return lastExpectedFpp;
    }


    /**
     * Convert a state into a full solution, a list
     * states ending in this state if it is a solution state,
     * or an empty list if it is not a solution state.
     */
    List<T> toSolution(T state) {
        return state.isSolution() ? StreamEx.iterate(
            state,
            s -> s != null,
            s -> s.predecessor().orElse(null)
        ).collect(toImmutableList()).reverse() : ImmutableList.of();
    }
}
