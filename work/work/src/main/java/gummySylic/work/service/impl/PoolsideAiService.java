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

/**
 * Free-tier Poolside model served through OpenRouter's OpenAI-compatible API.
 */
@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "poolside", matchIfMissing = true)
public class PoolsideAiService implements AiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String appName;
    private final String appUrl;

    public PoolsideAiService(
            @Value("${openrouter.api-key:}") String apiKey,
            @Value("${openrouter.model:poolside/laguna-xs-2.1:free}") String model,
            @Value("${openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${openrouter.app-name:GummySylic}") String appName,
            @Value("${openrouter.app-url:https://example.com}") String appUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.appName = appName;
        this.appUrl = appUrl;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String generateVeo3Prompt(String userRequest, String imageBase64, String imageMimeType, String promptType) {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of("type", "text", "text", "User request: " + userRequest));
        if (imageBase64 != null && !imageBase64.isBlank()) {
            String mime = imageMimeType != null ? imageMimeType : "image/jpeg";
            content.add(Map.of("type", "image_url", "image_url", Map.of(
                    "url", "data:" + mime + ";base64," + imageBase64)));
        }
        return callChat(Veo3Prompts.systemPromptFor(promptType), content).trim();
    }

    @Override
    public List<String> suggestPrompts(String topic, int count) {
        int n = Math.max(1, Math.min(count, 5));
        List<Map<String, Object>> content = List.of(Map.of(
                "type", "text", "text", "Topic: " + topic + "\nNumber of prompts: " + n));
        String raw = callChat(Veo3Prompts.SUGGEST_SYSTEM_PROMPT, content);
        return raw.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(n)
                .toList();
    }

    private String callChat(String systemPrompt, List<Map<String, Object>> userContent) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "openrouter.api-key is not configured. Set OPENROUTER_API_KEY environment variable.");
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContent)),
                "temperature", 0.8,
                // disable thinking so the whole budget goes to the answer
                "reasoning", Map.of("enabled", false),
                "max_tokens", 4096);

        String raw = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", appUrl)
                .header("X-Title", appName)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Empty response from OpenRouter API");
        }
        JsonNode response;
        try {
            response = MAPPER.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JSON from OpenRouter API: " + raw, e);
        }
        JsonNode textNode = response.at("/choices/0/message/content");
        // reasoning models may return content: null with the answer in "reasoning"
        if (textNode.isMissingNode() || textNode.isNull() || textNode.asText().isBlank()) {
            textNode = response.at("/choices/0/message/reasoning");
        }
        if (textNode.isMissingNode() || textNode.isNull() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Model returned no content. Raw response: " + raw);
        }
        return textNode.asText();
    }
}
