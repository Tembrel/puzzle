package net.peierls.puzzle;


/**
 * A container for states that offers only an approximate containment
 * test, no removal, and no iteration. The interface is modeled on the
 * idea of a Bloom filter and closely adheres to the Guava BloomFilter
 * API.
 * <p>
 * Implementations of this interface are free to make the containment
 * test exact, in which case:
 * <ul>
 * <li>
 * {@link #mightContain} will return true <em>iff</em> the given
 * state has been added;
 * </li>
 * <li>
 * the return value of {@link #put} will return true <em>iff</em> the
 * state was added for the first time;
 * </li>
 * <li>
 * {@link #expectedFalsePositiveProbability} will always return {@code 0.0};
 * and
 * </li>
 * <li>
 * {@link #approximateElementCount} will always be the true number of
 * distinct elements added.
 * </li>
 * </ul>
 */
public interface PuzzleStateFilter<T extends PuzzleState<T>> {

    /**
     * Returns true if the given state might have been put in
     * this filter, false if this is definitely not the case.
     */
    boolean mightContain(T state);

    /**
     * Adds the given state to the filter, returning true if
     * this was definitely the first time the state was added.
     * @returns true if this is definitely the first time the
     * state has been added to the tracker, false otherwise,
     * i.e., if this might not be the first time it has been
     * added.
     */
    boolean put(T state);

    /**
     * Returns an approximation of the number of distinct
     * states that have been added to the tracker.
     */
    long approximateElementCount();

    /**
     * Expected probability that {@link #mightContain} will return
     * a false positive.
     */
    double expectedFalsePositiveProbability();
}
