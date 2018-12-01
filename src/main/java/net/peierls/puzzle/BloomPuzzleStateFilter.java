package net.peierls.puzzle;

import com.google.common.hash.*;

import java.util.concurrent.*;


/**
 * An implementation of {@link PuzzleStateFilter} with approximate containment
 * that uses a BloomFilter internally.
 */
public class BloomPuzzleStateFilter<T extends PuzzleState<T>>
        implements PuzzleStateFilter<T> {

    private final BloomFilter<T> filter;


    BloomPuzzleStateFilter(Funnel<T> funnel, long expectedInsertions, double fpp) {
        this.filter = BloomFilter.create(funnel, expectedInsertions, fpp);
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
        return filter.expectedFpp();
    }
}
