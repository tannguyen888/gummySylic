package gummySylic.work.service;

import gummySylic.work.payload.dto.PromptDto;
import gummySylic.work.payload.request.GeneratePromptRequest;

import java.util.List;

public interface PromptService {

    PromptDto generatePrompt(GeneratePromptRequest request);

    List<PromptDto> getUserPrompts(Long telegramUserId);

    List<String> suggestPrompts(String topic, int count);
}
