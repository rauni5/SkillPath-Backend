package com.skillpath.ai;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skillpath.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * Thin wrapper around Gemini's generateContent REST API. This is the ONLY
 * class in the backend that talks to the AI provider directly — the API key
 * lives only here (via config), and callers work with plain Java types, so
 * swapping providers later means changing this one file, not the rest of
 * the app.
 */
@Component
public class GeminiClient {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;
    @Value("${gemini.model}")
    private String model;

    public GeminiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Plain-text chat reply, given a system instruction and the conversation so far. */
    public String chat(String systemInstruction, List<ChatTurn> history) {
        ObjectNode body = mapper.createObjectNode();
        body.set("systemInstruction", instructionNode(systemInstruction));
        body.set("contents", contentsNode(history));
        return extractText(call(body));
    }

    /** JSON generation with a strict response schema (e.g. quiz questions). Returns the raw JSON text. */
    public String generateJson(String systemInstruction, String userPrompt, JsonNode responseSchema) {
        ObjectNode body = mapper.createObjectNode();
        body.set("systemInstruction", instructionNode(systemInstruction));
        body.set("contents", contentsNode(List.of(ChatTurn.user(userPrompt))));

        ObjectNode generationConfig = mapper.createObjectNode();
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.set("responseSchema", responseSchema);
        body.set("generationConfig", generationConfig);

        return extractText(call(body));
    }

    private ObjectNode instructionNode(String text) {
        ObjectNode instruction = mapper.createObjectNode();
        instruction.putArray("parts").addObject().put("text", text);
        return instruction;
    }

    private ArrayNode contentsNode(List<ChatTurn> turns) {
        ArrayNode contents = mapper.createArrayNode();
        for (ChatTurn turn : turns) {
            ObjectNode entry = mapper.createObjectNode();
            entry.put("role", turn.role());
            entry.putArray("parts").addObject().put("text", turn.text());
            contents.add(entry);
        }
        return contents;
    }

    private JsonNode call(ObjectNode body) {
        if (apiKey == null || apiKey.isBlank())
            throw new AiServiceException("The AI tutor isn't configured yet — no Gemini API key is set.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            String url = BASE_URL + model + ":generateContent";
            String rawResponse = restTemplate.postForObject(url, request, String.class);
            return mapper.readTree(rawResponse);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(429)) {
                throw new AiServiceException(
                    "The AI tutor is getting a lot of requests right now — please try again in a moment.", e);
            }
            throw new AiServiceException("The AI tutor didn't respond correctly. Please try again.", e);
        } catch (ResourceAccessException e) {
            throw new AiServiceException(
                "Couldn't reach the AI tutor — please check your connection and try again.", e);
        } catch (Exception e) {
            throw new AiServiceException("Something went wrong talking to the AI tutor.", e);
        }
    }

    private String extractText(JsonNode response) {
        JsonNode textNode = response.path("candidates").path(0)
                .path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            // Could be a safety block or an empty candidate list — surface a clean message either way.
            throw new AiServiceException("The AI tutor couldn't generate a response. Please try again.");
        }
        return textNode.asText();
    }
}