package com.skillpath.algorithm.graph;
import java.util.*;
public class TopologicalSort {
    public List<Long> sort(SkillGraph graph, Set<Long> skills) {
        Map<Long, Integer> inDegree = new HashMap<>();
        Map<Long, List<Long>> adj = new HashMap<>();
        for (Long s : skills) { 
            inDegree.put(s, 0); adj.put(s, new ArrayList<>()); 
        }
        for (Long s : skills) {
            for (Long prereq : graph.getAdjacencyList().getOrDefault(s, List.of())) {
                if (!skills.contains(prereq)) continue;
                adj.get(prereq).add(s);
                inDegree.merge(s, 1, Integer::sum);
            }
        }
        Queue<Long> queue = new LinkedList<>();
        List<Long> result = new ArrayList<>();
        inDegree.forEach((id, deg) -> { 
            if (deg == 0) queue.add(id); 
        });
        while (!queue.isEmpty()) {
            Long cur = queue.poll();
            result.add(cur);
            for (Long next : adj.get(cur))
                if (inDegree.merge(next, -1, Integer::sum) == 0)
                queue.add(next);
        }
        if (result.size() != skills.size()) throw new IllegalStateException("Cycle detected in skill dependency graph");
        return result;
    }
}