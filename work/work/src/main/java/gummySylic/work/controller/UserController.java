package gummySylic.work.controller;

import gummySylic.work.common.exception.response.ApiResponse;
import gummySylic.work.payload.dto.UserDto;
import gummySylic.work.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{telegramUserId}")
    public ResponseEntity<ApiResponse<UserDto>> getByTelegramUserId(
            @PathVariable Long telegramUserId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getByTelegramUserId(telegramUserId)));
    }
}
