package com.skillpath.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skillpath.ai.GeminiClient;
import com.skillpath.dto.response.SkillCheckGenerateResponse;
import com.skillpath.dto.response.SkillCheckQuestionResponse;
import com.skillpath.dto.response.SkillCheckResultResponse;
import com.skillpath.exception.AiServiceException;
import com.skillpath.exception.ForbiddenException;
import com.skillpath.exception.ResourceNotFoundException;
import com.skillpath.model.Skill.Skill;
import com.skillpath.model.SkillCheckAttempt.SkillCheckAttempt;
import com.skillpath.model.enums.Proficiency;
import com.skillpath.model.enums.SkillCheckStatus;
import com.skillpath.repository.SkillCheckAttemptRepository;
import com.skillpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
public class SkillCheckService {
    private static final int QUESTION_COUNT = 5;
    private static final int POINTS_PER_QUESTION = 4; // 5 * 4 = 20, matching the 0-20 scoring scale

    private final SkillCheckAttemptRepository attemptRepo;
    private final SkillRepository skillRepo;
    private final RoadmapService roadmapService;
    private final GeminiClient geminiClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public SkillCheckGenerateResponse generate(Long userId, Long skillId) {
        Skill skill = skillRepo.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));

        String systemInstruction =
                "You are an expert technical assessor. You create fair, practical multiple-choice " +
                "quizzes that test real understanding of a skill, not trivia. Every question must have " +
                "exactly 4 options and exactly one correct answer. Vary difficulty across the questions, " +
                "from foundational to applied. Return only what the schema asks for.";
        String userPrompt = "Generate exactly " + QUESTION_COUNT +
                " multiple-choice questions to test practical knowledge of \"" + skill.getName() +
                "\" (category: " + skill.getCategory() + ")." +
                (skill.getDescription() != null && !skill.getDescription().isBlank()
                        ? " Context: " + skill.getDescription() + "." : "");

        String rawJson = geminiClient.generateJson(systemInstruction, userPrompt, buildQuestionSchema());
        JsonNode parsed = parseOrFail(rawJson);
        JsonNode qArray = parsed.path("questions");
        if (!qArray.isArray() || qArray.isEmpty())
            throw new AiServiceException("The AI tutor returned an empty quiz. Please try again.");

        SkillCheckAttempt attempt = attemptRepo.save(SkillCheckAttempt.builder()
                .userId(userId).skillId(skillId)
                .questionsJson(rawJson)
                .status(SkillCheckStatus.GENERATED)
                .build());

        List<SkillCheckQuestionResponse> questions = new ArrayList<>();
        for (int i = 0; i < qArray.size(); i++) {
            JsonNode q = qArray.get(i);
            List<String> options = new ArrayList<>();
            q.path("options").forEach(o -> options.add(o.asText()));
            questions.add(SkillCheckQuestionResponse.builder()
                    .index(i)
                    .question(q.path("question").asText())
                    .options(options)
                    .build());
        }

        return SkillCheckGenerateResponse.builder()
                .attemptId(attempt.getId())
                .questions(questions)
                .build();
    }

    @Transactional
    public SkillCheckResultResponse submit(Long userId, Long attemptId, List<Integer> answers) {
        SkillCheckAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));
        if (!attempt.getUserId().equals(userId))
            throw new ForbiddenException("This isn't your skill check.");
        if (attempt.getStatus() == SkillCheckStatus.SUBMITTED)
            throw new IllegalStateException("This skill check was already submitted.");

        JsonNode parsed = parseOrFail(attempt.getQuestionsJson());
        JsonNode qArray = parsed.path("questions");

        int score = 0;
        List<Boolean> correctness = new ArrayList<>();
        for (int i = 0; i < qArray.size(); i++) {
            int correctIndex = qArray.get(i).path("correctIndex").asInt(-1);
            Integer given = i < answers.size() ? answers.get(i) : null;
            boolean correct = given != null && given == correctIndex;
            correctness.add(correct);
            if (correct) score += POINTS_PER_QUESTION;
        }

        Proficiency earned = scoreToProficiency(score);
        boolean passed = earned != null;

        attempt.setStatus(SkillCheckStatus.SUBMITTED);
        attempt.setScore(score);
        attempt.setProficiency(earned);
        attempt.setSubmittedAt(Instant.now());
        attemptRepo.save(attempt);

        if (passed) {
            roadmapService.completeStepsForSkill(userId, attempt.getSkillId(), earned);
        }

        return SkillCheckResultResponse.builder()
                .score(score)
                .maxScore(QUESTION_COUNT * POINTS_PER_QUESTION)
                .proficiency(earned != null ? earned.name() : null)
                .passed(passed)
                .correctness(correctness)
                .build();
    }

    /** 0-9: not yet passed · 10-14: Beginner · 15-19: Intermediate · 20: Advanced */
    private Proficiency scoreToProficiency(int score) {
        if (score >= 20) return Proficiency.ADVANCED;
        if (score >= 15) return Proficiency.INTERMEDIATE;
        if (score >= 10) return Proficiency.BEGINNER;
        return null;
    }

    private JsonNode parseOrFail(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new AiServiceException("Something went wrong reading this skill check. Please try again.");
        }
    }

    private JsonNode buildQuestionSchema() {
        ObjectNode optionItem = mapper.createObjectNode();
        optionItem.put("type", "STRING");

        ObjectNode options = mapper.createObjectNode();
        options.put("type", "ARRAY");
        options.set("items", optionItem);

        ObjectNode correctIndex = mapper.createObjectNode();
        correctIndex.put("type", "INTEGER");

        ObjectNode question = mapper.createObjectNode();
        question.put("type", "STRING");

        ObjectNode questionProps = mapper.createObjectNode();
        questionProps.set("question", question);
        questionProps.set("options", options);
        questionProps.set("correctIndex", correctIndex);

        ObjectNode questionItem = mapper.createObjectNode();
        questionItem.put("type", "OBJECT");
        questionItem.set("properties", questionProps);
        ArrayNode questionRequired = mapper.createArrayNode();
        questionRequired.add("question").add("options").add("correctIndex");
        questionItem.set("required", questionRequired);

        ObjectNode questionsArray = mapper.createObjectNode();
        questionsArray.put("type", "ARRAY");
        questionsArray.set("items", questionItem);

        ObjectNode rootProps = mapper.createObjectNode();
        rootProps.set("questions", questionsArray);

        ObjectNode root = mapper.createObjectNode();
        root.put("type", "OBJECT");
        root.set("properties", rootProps);
        ArrayNode rootRequired = mapper.createArrayNode();
        rootRequired.add("questions");
        root.set("required", rootRequired);
        return root;
    }
}