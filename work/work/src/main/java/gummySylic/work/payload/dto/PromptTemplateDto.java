package gummySylic.work.payload.dto;

import java.time.LocalDateTime;

public record PromptTemplateDto(

        Long id,
        String name,
        String category,
        String promptType,
        String templateContent,
        Boolean isActive,
        LocalDateTime createdAt

) {
}
