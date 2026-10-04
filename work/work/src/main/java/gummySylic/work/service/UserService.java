package gummySylic.work.service;

import gummySylic.work.modal.User;
import gummySylic.work.payload.dto.UserDto;

public interface UserService {

    User getOrCreateUser(Long telegramUserId, String username, String firstName);

    UserDto getByTelegramUserId(Long telegramUserId);
}
