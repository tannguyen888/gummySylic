package gummySylic.work.payload.dto;

import java.time.LocalDateTime;

public record PromptDto(

                Long id,
                Long userId,
                String originalRequest,
                String generatedPrompt,
                String promptType,
                String category,
                LocalDateTime createdAt

) {
}