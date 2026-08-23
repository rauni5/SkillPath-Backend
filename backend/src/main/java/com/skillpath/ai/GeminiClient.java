package com.skillpath.ai;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skillpath.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    /** 3 attempts total = 1 initial try + 2 retries. Keeps worst-case added
     *  latency to a few seconds rather than leaving the user staring at a
     *  spinner indefinitely while we hammer an already-overloaded API. */
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 800;
    private static final long MAX_DELAY_MS = 6000;

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

    /** Calls Gemini, retrying on transient failures (429 rate-limit, 5xx
     *  server-side) with exponential backoff + jitter, honoring a
     *  Retry-After header when Gemini sends one. Non-transient failures
     *  (bad request, auth, parsing) fail immediately — retrying those
     *  would just waste the remaining attempts on something that can't
     *  succeed. */
    private JsonNode call(ObjectNode body) {
        if (apiKey == null || apiKey.isBlank())
            throw new AiServiceException("The AI tutor isn't configured yet — no Gemini API key is set.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        String url = BASE_URL + model + ":generateContent";

        AiServiceException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String rawResponse = restTemplate.postForObject(url, request, String.class);
                return mapper.readTree(rawResponse);
            } catch (HttpStatusCodeException e) {
                int status = e.getStatusCode().value();
                lastFailure = messageFor(status, e);
                boolean retryable = status == 429 || (status >= 500 && status <= 504);
                if (!retryable || attempt == MAX_ATTEMPTS) throw lastFailure;
                sleepBeforeRetry(attempt, e);
            } catch (ResourceAccessException e) {
                lastFailure = new AiServiceException(
                    "Couldn't reach the AI service — please check your connection and try again.", e);
                if (attempt == MAX_ATTEMPTS) throw lastFailure;
                sleepBeforeRetry(attempt, null);
            } catch (Exception e) {
                // Not a rate-limit/availability issue (e.g. malformed response) — retrying
                // won't help, so fail immediately instead of burning the remaining attempts.
                throw new AiServiceException("Something went wrong talking to the AI. Please try again.", e);
            }
        }
        // Unreachable — the loop always either returns or throws — but keeps the compiler happy.
        throw lastFailure;
    }

    /** Distinguishes *why* a request failed so the retry banner in the app
     *  says something accurate instead of a generic "something went
     *  wrong" for what's actually a busy API. */
    private AiServiceException messageFor(int status, HttpStatusCodeException e) {
        if (status == 429) {
            return new AiServiceException(
                "The AI is handling a lot of requests right now — please wait a moment and try again.", e);
        }
        if (status == 503) {
            return new AiServiceException(
                "The AI service is temporarily unavailable — please try again shortly.", e);
        }
        if (status >= 500) {
            return new AiServiceException(
                "The AI service is having trouble responding right now. Please try again.", e);
        }
        return new AiServiceException("The AI didn't respond correctly. Please try again.", e);
    }

    private void sleepBeforeRetry(int attempt, HttpStatusCodeException e) {
        long delay = retryAfterMillis(e);
        if (delay <= 0) {
            long backoffCeiling = Math.min(MAX_DELAY_MS, BASE_DELAY_MS * (1L << (attempt - 1)));
            long half = Math.max(1, backoffCeiling / 2);
            delay = half + ThreadLocalRandom.current().nextLong(half);
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /** Gemini doesn't always send this, but honor it when present instead
     *  of guessing with backoff — it knows its own quota reset timing
     *  better than we do. */
    private long retryAfterMillis(HttpStatusCodeException e) {
        if (e == null || e.getResponseHeaders() == null) return -1;
        List<String> values = e.getResponseHeaders().get("Retry-After");
        if (values == null || values.isEmpty()) return -1;
        try {
            return Long.parseLong(values.get(0).trim()) * 1000L;
        } catch (NumberFormatException nfe) {
            return -1;
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