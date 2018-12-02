package net.peierls.puzzle;

import java.util.concurrent.*;

/**
 * An implementation of {@link PuzzleStateFilter} with exact containment.
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
public class ExactPuzzleStateFilter<T extends PuzzleState<T>>
        implements PuzzleStateFilter<T> {

    private final ConcurrentMap<T, T> map = new ConcurrentHashMap<>();


    @Override
    public boolean mightContain(T state) {
        return map.containsKey(state);
    }

    @Override
    public boolean put(T state) {
        return map.putIfAbsent(state, state) == null;
    }

    @Override
    public long approximateElementCount() {
        return map.size();
    }

    @Override
    public double expectedFalsePositiveProbability() {
        return 0.0;
    }
}
