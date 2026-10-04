package gummySylic.work.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GeneratePromptRequest(

        @NotNull Long telegramUserId,
        String username,
        String firstName,
        @NotBlank String request,
        String imageBase64,
        String imageMimeType,
        String promptType,
        String category

) {
}
