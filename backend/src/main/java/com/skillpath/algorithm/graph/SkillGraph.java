package com.skillpath.algorithm.graph;
import java.util.*;
public class SkillGraph {
    private final Map<Long, List<Long>> adjacencyList = new HashMap<>();
    public void addSkill(Long skillId) {
        adjacencyList.putIfAbsent(skillId, new ArrayList<>());
    }
    public void addDependency(Long skillId, Long prerequisiteId) {
        adjacencyList.computeIfAbsent(skillId, k -> new ArrayList<>()).add(prerequisiteId);
        adjacencyList.putIfAbsent(prerequisiteId, new ArrayList<>());
    }
    public Set<Long> getAllPrerequisites(Long targetSkillId) {
        Set<Long> visited = new LinkedHashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(targetSkillId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            for (Long prereq : adjacencyList.getOrDefault(current, List.of())) if (visited.add(prereq)) queue.add(prereq);
        }
        return visited;
    }
    public Set<Long> getMissingSkills(Set<Long> userSkillIds, Set<Long> requiredSkillIds) {
        Set<Long> needed = new LinkedHashSet<>();
        for (Long req : requiredSkillIds) {
            if (!userSkillIds.contains(req)) {
                needed.add(req);
                needed.addAll(getAllPrerequisites(req));
            }
        }
        needed.removeAll(userSkillIds);
        return needed;
    }
    public Map<Long, List<Long>> getAdjacencyList() { 
        return adjacencyList; 
    }
}
