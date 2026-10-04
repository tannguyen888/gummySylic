package gummySylic.work.controller;

import gummySylic.work.common.exception.response.ApiResponse;
import gummySylic.work.payload.dto.PromptDto;
import gummySylic.work.payload.request.GeneratePromptRequest;
import gummySylic.work.service.PromptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PromptDto>> generate(
            @Valid @RequestBody GeneratePromptRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(promptService.generatePrompt(request)));
    }

    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<String>>> suggest(
            @RequestParam String topic,
            @RequestParam(defaultValue = "3") int count) {
        return ResponseEntity.ok(ApiResponse.ok(promptService.suggestPrompts(topic, count)));
    }

    @GetMapping("/user/{telegramUserId}")
    public ResponseEntity<ApiResponse<List<PromptDto>>> getUserPrompts(
            @PathVariable Long telegramUserId) {
        return ResponseEntity.ok(ApiResponse.ok(promptService.getUserPrompts(telegramUserId)));
    }
}
