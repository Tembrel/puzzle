package net.peierls.puzzle.client;

import com.google.common.graph.SuccessorsFunction;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import one.util.streamex.*;


/**
 * Demonstrate usage of puzzle framework.
 */
public class SampleUsage {

    public static class Node {
        public final long n;
        Node(long n) { this.n = n; }
        public static Node of(long n) { return new Node(n); }
        public static Iterable<? extends Node> successors(Node node) {
            return StreamEx.of(
                Node.of(3 * node.n - 1),
                Node.of(node.n / 2)
            );
        }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            return this.n == ((Node) obj).n;
        }
        @Override public int hashCode() {
            return Objects.hash(n);
        }
    }

    public static <T> Stream<T> bfs(T start, SuccessorsFunction<T> graph) {
        Map<T, Integer> seen = new HashMap<>();
        Queue<T> queue = new ArrayDeque<>();
        queue.offer(start);
        return StreamEx.produce(action -> {
            T node = queue.poll();
            if (node == null) {
                return false;
            } else {
                action.accept(node);
                for (T succ : graph.successors(node)) {
                    if (seen.merge(succ, 1, Integer::sum) == 1) {
                        queue.offer(succ);
                    }
                }
                return true;
            }
        });
    }

    public static void main(String... args) {
        int limit = 100;
        Stream<Node> nodes = bfs(Node.of(1), Node::successors);
        System.out.printf("first %d nodes: %s%n", limit,
            StreamEx.of(nodes).limit(limit).mapToLong(node -> node.n).joining(", "));
    }
}
