package com.skillpath.algorithm.trie;
import java.util.*;
public class SkillTrie {
    private static class TrieNode {
        final Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd; 
        Long skillId;
    }
    private final TrieNode root = new TrieNode();
  
    public void insert(String name, Long skillId) {
        TrieNode node = root;
        for (char c : name.toLowerCase().toCharArray())
        node = node.children.computeIfAbsent(c, k -> new TrieNode());
        node.isEnd = true;
        node.skillId = skillId;
    }
    
    public List<String> searchByPrefix(String prefix, int limit) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.children.get(c);
            if (node == null) return List.of();
        }
        List<String> results = new ArrayList<>();
        collect(node, new StringBuilder(prefix), results, limit);
        return results;
    }
    private void collect(TrieNode node, StringBuilder sb,
    List<String> out, int limit) {
        if (out.size() >= limit) return;
        if (node.isEnd) out.add(sb.toString());
        for (var e : node.children.entrySet()) {
            sb.append(e.getKey());
            collect(e.getValue(), sb, out, limit);
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}