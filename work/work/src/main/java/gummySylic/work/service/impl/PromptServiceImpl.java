package gummySylic.work.service.impl;

import gummySylic.work.mapper.PromptMapper;
import gummySylic.work.modal.Prompt;
import gummySylic.work.modal.User;
import gummySylic.work.payload.dto.PromptDto;
import gummySylic.work.payload.request.GeneratePromptRequest;
import gummySylic.work.repository.PromptRepository;
import gummySylic.work.service.AiService;
import gummySylic.work.service.PromptService;
import gummySylic.work.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;
    private final UserService userService;
    private final AiService aiService;

    @Override
    @Transactional
    public PromptDto generatePrompt(GeneratePromptRequest request) {
        User user = userService.getOrCreateUser(
                request.telegramUserId(), request.username(), request.firstName());

        String promptType = request.promptType() != null ? request.promptType() : "veo3-video";
        String generated = aiService.generateVeo3Prompt(
                request.request(), request.imageBase64(), request.imageMimeType(), promptType);

        Prompt prompt = Prompt.builder()
                .user(user)
                .originalRequest(request.request())
                .generatedPrompt(generated)
                .promptType(promptType)
                .category(request.category())
                .imageUrlAnalytical(request.imageBase64() != null ? "inline-image" : null)
                .build();

        return PromptMapper.toPromptDto(promptRepository.save(prompt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromptDto> getUserPrompts(Long telegramUserId) {
        return promptRepository.findByUser_TelegramUserIdOrderByCreatedAtDesc(telegramUserId)
                .stream()
                .map(PromptMapper::toPromptDto)
                .toList();
    }

    @Override
    public List<String> suggestPrompts(String topic, int count) {
        return aiService.suggestPrompts(topic, count);
    }
}
