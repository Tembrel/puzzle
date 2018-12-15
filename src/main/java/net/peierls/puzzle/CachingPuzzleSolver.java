package net.peierls.puzzle;

import com.google.common.collect.ImmutableList;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import one.util.streamex.StreamEx;


/**
 * Abstract base for puzzle solvers that use a {@link PuzzleStateCache}
 * to keep track (possibility only approximately) of which states have
 * been seen during the search for a solution. General-purpose solvers
 * necessarily fall into this category, because otherwise there is no
 * way to know in general whether a particular state has already been
 * examined. (Custom solvers for specific puzzle state types might be
 * able to get around this with domain-specific information.)
 * <p>
 * Because in general the search space can be so large that it is not
 * possible to store all states that have been seen (even taking advantage
 * of compressed forms), the {@link PuzzleStateCache} abstraction is
 * used to allow for searches that use much less memory but that may fail
 * occasionally to search a valid branch.
 * <p>
 * The {@link #solution} method is final; subclasses should implement
 * {@link #solutionState solutionState(initialState, cache)} and use the
 * {@link #filterState filterState(state, cache)} method before searching a state.
 */
public abstract class CachingPuzzleSolver<T extends PuzzleState<T>> implements PuzzleSolver<T> {

    private final Supplier<PuzzleStateCache<T>> cacheSupplier;


    /**
     * Constructs a solver that will always use an exact (non-lossy) cache,
     * which will store each state seen but will always report correctly on
     * whether a state has been seen.
     */
    protected CachingPuzzleSolver() {
        this.cacheSupplier = ExactPuzzleStateCache::new;
    }

    /**
     * Constructs a solver that will use the cache supplier to provide
     * caches for puzzle states.
     */
    protected CachingPuzzleSolver(Supplier<PuzzleStateCache<T>> cacheSupplier) {
        this.cacheSupplier = cacheSupplier;
    }


    @Override
    public final List<T> solution(T initialState) {
        if (initialState == null) {
            throw new NullPointerException("initial state must not be null");
        }
        try (PuzzleStateCache<T> cache = cacheSupplier.get()) {
            if (cache == null) {
                throw new IllegalStateException("cache supplier must not return null");
            }
            return solutionState(initialState, cache)
                .map(this::toSolution)
                .orElseGet(Collections::emptyList);
        }
    }


    /**
     * Concrete subclasses must implement this method to
     * find a solution from the given initial state, using
     * the given cache.
     */
    protected abstract Optional<T> solutionState(T initialState, PuzzleStateCache<T> cache);


    /**
     * If state is neither null nor hopeless and definitely hasn't
     * been seen by the cache, returns an initialized copy of state
     * (and as a side-effect marks state as seen in the cache),
     * otherwise returns null. The test for hopelessness is made
     * on the initialized copy.
     */
    protected T filterState(T state, PuzzleStateCache<T> cache) {
        if (state == null || !cache.put(state)) {
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
    protected Stream<T> successors(T state, PuzzleStateCache<T> cache) {
        return StreamEx.of(state.successors())
            .map(s -> filterState(s, cache))
            .nonNull();
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


    /**
     * For debugging.
     */
    protected void trace(T state) {
        System.out.printf("searching %s, pred %s%n",
            state,
            state.predecessor().map(Object::toString).orElse("-"));
    }
}
