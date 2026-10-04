package gummySylic.work.payload.dto;

import java.time.LocalDateTime;

public record UserDto(

                Long id,
                Long telegramUserId,
                String username,
                String firstName,
                Boolean isActive,
                LocalDateTime createdAt

) {
}