package net.peierls.puzzle;

import com.google.common.hash.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.stream.*;

import one.util.streamex.*;

import org.junit.*;
import static org.junit.Assert.*;


public class DfsPuzzleSolverTest {

    final static int INIT = 1;
    final static int FINAL = 7;

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

        @Override public Optional<Funnel<CounterState>> funnel() {
            return Optional.of((state, sink) -> sink.putLong(state.count));
        }
    }

    @Test public void dfs() {
        CounterState initialState = new CounterState(INIT, null);
        PuzzleSolver<CounterState> solver = new DfsPuzzleSolver<>();
        Optional<List<CounterState>> solution = solver.solution(initialState);
        assertTrue(solution.isPresent());
        String solutionString = StreamEx.of(solution.get())
            .mapToLong(CounterState::getCount)
            .joining(", ", "[", "]");
        System.out.println("solved: " + solutionString);
    }
}
