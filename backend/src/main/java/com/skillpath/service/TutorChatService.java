package com.skillpath.service;
import com.skillpath.ai.ChatTurn;
import com.skillpath.ai.GeminiClient;
import com.skillpath.dto.response.ChatMessageResponse;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.ChatMessage.ChatMessage;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.enums.MessageRole;
import com.skillpath.repository.ChatMessageRepository;
import com.skillpath.repository.SkillRepository;
import com.skillpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service @RequiredArgsConstructor
public class TutorChatService {
    private static final int MAX_CONTEXT_MESSAGES = 6;

    private final ChatMessageRepository chatRepo;
    private final SkillRepository skillRepo;
    private final UserSkillRepository userSkillRepo;
    private final GeminiClient geminiClient;

    public List<ChatMessageResponse> getHistory(Long userId, Long skillId) {
        return chatRepo.findByUserIdAndSkillIdOrderByCreatedAtAsc(userId, skillId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long skillId, String userMessage) {
        Skill skill = skillRepo.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));

        chatRepo.save(ChatMessage.builder()
                .userId(userId).skillId(skillId)
                .role(MessageRole.USER).content(userMessage)
                .build());

        String systemInstruction = buildSystemInstruction(userId, skill);
        List<ChatTurn> turns = recentTurns(userId, skillId);

        String reply = geminiClient.chat(systemInstruction, turns);

        ChatMessage saved = chatRepo.save(ChatMessage.builder()
                .userId(userId).skillId(skillId)
                .role(MessageRole.ASSISTANT).content(reply)
                .build());
        return toResponse(saved);
    }

    private List<ChatTurn> recentTurns(Long userId, Long skillId) {
        List<ChatMessage> recent = new ArrayList<>(chatRepo.findByUserIdAndSkillIdOrderByCreatedAtDesc(
                userId, skillId, PageRequest.of(0, MAX_CONTEXT_MESSAGES)));
        Collections.reverse(recent); // back into chronological order
        return recent.stream()
                .map(m -> m.getRole() == MessageRole.USER ? ChatTurn.user(m.getContent()) : ChatTurn.model(m.getContent()))
                .toList();
    }

    private String buildSystemInstruction(Long userId, Skill skill) {
        Set<Long> knownSkillIds = userSkillRepo.findSkillIdsByUserId(userId);
        List<String> knownNames = skillRepo.findAllById(knownSkillIds).stream()
                .map(Skill::getName)
                .filter(name -> !name.equalsIgnoreCase(skill.getName()))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("You are a friendly, patient tutor helping a student learn \"")
          .append(skill.getName()).append("\" (category: ").append(skill.getCategory()).append("). ");
        if (skill.getDescription() != null && !skill.getDescription().isBlank())
            sb.append("Context on this skill: ").append(skill.getDescription()).append(". ");
        if (!knownNames.isEmpty())
            sb.append("The student already knows: ").append(String.join(", ", knownNames))
              .append(" — you can reference these when useful, no need to re-explain them from scratch. ");
        sb.append("Keep answers concise and practical, use short code examples where they help, and check ")
          .append("understanding with the occasional question. This is a learning conversation, not a test — ")
          .append("don't ask the student to submit graded work here; a separate skill check handles that.");
        return sb.toString();
    }

    private ChatMessageResponse toResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .role(m.getRole().name())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}