package com.skillpath.algorithm.heap;
import java.util.*;
public class RecommendationHeap<T> {
    
    public record ScoredItem<T>(T item, double score) implements Comparable<ScoredItem<T>> {
        @Override
        public int compareTo(ScoredItem<T> o) {
            return Double.compare(o.score, this.score);
        }
    }
    private final PriorityQueue<ScoredItem<T>> pq = new PriorityQueue<>();
    
    public void push(T item, double score) {
        pq.offer(new ScoredItem<>(item, score));
    }
    
    public List<ScoredItem<T>> getTopN(int n) {
        PriorityQueue<ScoredItem<T>> copy = new PriorityQueue<>(pq);
        List<ScoredItem<T>> result = new ArrayList<>();
        while (!copy.isEmpty() && result.size() < n)
            result.add(copy.poll());
        return result;
    }
    
    public boolean isEmpty() { 
        return pq.isEmpty(); 
    }

}
