package gummySylic.work.mapper;

import gummySylic.work.modal.Prompt;
import gummySylic.work.payload.dto.PromptDto;

public class PromptMapper {

    private PromptMapper() {
    }

    public static PromptDto toPromptDto(Prompt prompt) {
        if (prompt == null) {
            return null;
        }
        return new PromptDto(
                prompt.getId(),
                prompt.getUser() != null ? prompt.getUser().getId() : null,
                prompt.getOriginalRequest(),
                prompt.getGeneratedPrompt(),
                prompt.getPromptType(),
                prompt.getCategory(),
                prompt.getCreatedAt());
    }
}
