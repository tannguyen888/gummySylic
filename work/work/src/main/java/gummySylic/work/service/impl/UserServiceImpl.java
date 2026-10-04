package gummySylic.work.service.impl;

import gummySylic.work.common.exception.ResourceNotFound;
import gummySylic.work.mapper.UserMapper;
import gummySylic.work.modal.User;
import gummySylic.work.payload.dto.UserDto;
import gummySylic.work.repository.UserRepository;
import gummySylic.work.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getOrCreateUser(Long telegramUserId, String username, String firstName) {
        return userRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .telegramUserId(telegramUserId)
                        .username(username)
                        .firstName(firstName)
                        .isActive(true)
                        .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getByTelegramUserId(Long telegramUserId) {
        User user = userRepository.findByTelegramUserId(telegramUserId)
                .orElseThrow(() -> new ResourceNotFound(
                        "User not found with telegramUserId: " + telegramUserId));
        return UserMapper.toUserDto(user);
    }
}
