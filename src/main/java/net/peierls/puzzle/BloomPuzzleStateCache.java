package net.peierls.puzzle;

import com.google.common.hash.*;

import java.util.concurrent.*;


/**
 * An implementation of {@link PuzzleStateCache} with approximate containment
 * that uses a BloomFilter internally.
 */
public class BloomPuzzleStateCache<T extends PuzzleState<T>>
        implements PuzzleStateCache<T> {

    private final BloomFilter<T> cache;


    public BloomPuzzleStateCache(Funnel<T> funnel, long expectedInsertions, double fpp) {
        this.cache = BloomFilter.create(funnel, expectedInsertions, fpp);
    }


    @Override
    public boolean mightContain(T state) {
        return cache.mightContain(state);
    }

    @Override
    public boolean put(T state) {
        return cache.put(state);
    }

    @Override
    public long approximateElementCount() {
        return cache.approximateElementCount();
    }

    @Override
    public double expectedFalsePositiveProbability() {
        return cache.expectedFpp();
    }

    @Override
    public void close() {
        System.out.printf("Cache holds approx. %d elements with expected FPP %f:%n",
            approximateElementCount(), expectedFalsePositiveProbability());
    }
}
