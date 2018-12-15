package net.peierls.puzzle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An implementation of {@link PuzzleStateCache} with exact containment.
 * It has these properties:
 * <ul>
 * <li>
 * {@link #mightContain} returns true <em>iff</em> the given
 * state has been added;
 * </li>
 * <li>
 * the return value of {@link #put} reflects whether the
 * state was added for the first time;
 * </li>
 * <li>
 * {@link #expectedFalsePositiveProbability} always returns {@code 0.0};
 * and
 * </li>
 * <li>
 * {@link #approximateElementCount} is always the true number of
 * distinct elements added.
 * </li>
 * </ul>
 */
public class ExactPuzzleStateCache<T extends PuzzleState<T>>
        implements PuzzleStateCache<T> {

    private final Map<T, Long> seen = new ConcurrentHashMap<>();


    @Override
    public boolean mightContain(T state) {
        return seen.containsKey(state);
    }

    @Override
    public boolean put(T state) {
        return seen.merge(state, 1L, Long::sum) == 1L;
    }

    @Override
    public long approximateElementCount() {
        return seen.size();
    }

    @Override
    public double expectedFalsePositiveProbability() {
        return 0.0;
    }

    @Override
    public void close() {
        System.out.printf("Cache holds %d elements%n", seen.size());
    }
}
