package net.peierls.puzzle;

import com.google.common.base.Stopwatch;
import com.google.common.hash.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.stream.*;

import one.util.streamex.*;

import org.junit.*;
import static org.junit.Assert.*;


public class BfsPuzzleSolverTest {

    final static long INIT = 1L;
    final static long FINAL = 1_000L;

    static class CounterState implements PuzzleState<CounterState> {
        final long count;
        final CounterState pred;

        CounterState(long count, CounterState pred) {
            this.count = count;
            this.pred = pred;
        }

        public long getCount() { return count; }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CounterState)) return false;
            return count == ((CounterState)obj).count;
        }
        @Override public int hashCode() { return Objects.hashCode(count); }
        @Override public String toString() { return "" + count; }

        @Override public boolean isHopeless() { return count == 0; }
        @Override public boolean isSolution() { return count == FINAL; }
        @Override public Optional<CounterState> predecessor() {
            return Optional.ofNullable(pred);
        }
        @Override public Stream<CounterState> successors() {
            return LongStreamEx.of(count * 3 - 1, count / 2)
                .remove(c -> c == count)
                .mapToObj(c -> new CounterState(c, this));
        }

        static Funnel<CounterState> funnel() {
            return (state, sink) -> sink.putLong(state.count);
        }
    }

    @Test public void bfs() {
        CounterState initialState = new CounterState(INIT, null);
        FilteredPuzzleSolver<CounterState> solver = new BfsPuzzleSolver<>(
            () -> new BloomPuzzleStateFilter<CounterState>(CounterState.funnel(), 2_453_203, 0.0001)
        );
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<List<CounterState>> solution = solver.solution(initialState);
        if (solution.isPresent()) {
            List<Long> moves = StreamEx.of(solution.get())
                .mapToLong(CounterState::getCount)
                .boxed()
                .toList();
            System.out.printf(
                "solved in %s with %d moves%n",
                stopwatch,
                moves.size()
            );
        } else {
            System.out.printf(
                "no solution found in %s%n",
                stopwatch
            );
        }
    }
}
