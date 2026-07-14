package com.skillpath.service;
import com.skillpath.algorithm.trie.SkillTrie;
import com.skillpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class SkillTrieService implements ApplicationRunner {
    private final SkillRepository skillRepo;
    private final SkillTrie trie = new SkillTrie();
    @Override
    public void run(ApplicationArguments args) {
        // Populates the Trie once at startup from the skills table
        skillRepo.findAll().forEach(s -> trie.insert(s.getName(), s.getId()));
    }
    public List<String> autocomplete(String prefix) {
        return trie.searchByPrefix(prefix, 10);
    }
}
