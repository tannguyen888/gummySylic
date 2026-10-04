package gummySylic.work.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gummySylic.work.service.AiService;
import gummySylic.work.service.Veo3Prompts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiAiService implements AiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiAiService(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.0-flash}") String model,
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String generateVeo3Prompt(String userRequest, String imageBase64, String imageMimeType, String promptType) {
        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", "User request: " + userRequest));
        if (imageBase64 != null && !imageBase64.isBlank()) {
            parts.add(Map.of("inline_data", Map.of(
                    "mime_type", imageMimeType != null ? imageMimeType : "image/jpeg",
                    "data", imageBase64)));
        }
        return callGemini(Veo3Prompts.systemPromptFor(promptType), parts).trim();
    }

    @Override
    public List<String> suggestPrompts(String topic, int count) {
        int n = Math.max(1, Math.min(count, 5));
        List<Map<String, Object>> parts = List.of(
                Map.of("text", "Topic: " + topic + "\nNumber of prompts: " + n));
        String raw = callGemini(Veo3Prompts.SUGGEST_SYSTEM_PROMPT, parts);
        return raw.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(n)
                .toList();
    }

    private String callGemini(String systemPrompt, List<Map<String, Object>> userParts) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "gemini.api-key is not configured. Set GEMINI_API_KEY environment variable.");
        }
        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of("role", "user", "parts", userParts)),
                "generationConfig", Map.of("temperature", 0.8, "maxOutputTokens", 1024));

        String raw = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Empty response from Gemini API");
        }
        JsonNode response;
        try {
            response = MAPPER.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JSON from Gemini API: " + raw, e);
        }
        JsonNode textNode = response.at("/candidates/0/content/parts/0/text");
        if (textNode.isMissingNode()) {
            throw new IllegalStateException("Unexpected Gemini API response: " + raw);
        }
        return textNode.asText();
    }
}
