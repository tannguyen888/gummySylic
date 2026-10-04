package gummySylic.work.mapper;

import gummySylic.work.modal.User;
import gummySylic.work.payload.dto.UserDto;

public class UserMapper {

    private UserMapper() {
    }

    public static UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getTelegramUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getIsActive(),
                user.getCreatedAt());
    }
}
