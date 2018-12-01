package net.peierls.puzzle;

import java.util.stream.*;

import one.util.streamex.*;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Demonstrate usage of puzzle framework.
 */
public class ExactPuzzleStateFilterTest {

    static class NullState implements PuzzleState<NullState> {
        final String text;
        NullState(String text) { this.text = text; }
        public String getText() { return text; }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof NullState)) return false;
            return text.equals(((NullState)obj).text);
        }
        @Override public int hashCode() { return text.hashCode(); }
        @Override public boolean isSolution() { return false; }
        @Override public boolean isHopeless() { return false; }
        @Override public Stream<NullState> successors() { return StreamEx.of(); }
    }

    PuzzleStateFilter<NullState> f = new ExactPuzzleStateFilter<>();

    @Test public void exactFilter() {
        NullState abc = new NullState("abc");
        boolean firstTime = f.put(abc);
        assertTrue(firstTime);

        NullState abc2 = new NullState("abc");
        boolean contained = f.mightContain(abc2);
        assertTrue(contained);

        long size = f.approximateElementCount();
        assertEquals(1L, size);

        double fpp = f.expectedFalsePositiveProbability();
        assertEquals(0f, fpp, 0);
    }
}