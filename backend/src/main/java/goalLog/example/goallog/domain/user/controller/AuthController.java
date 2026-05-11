package goalLog.example.goallog.domain.user.controller;

import goalLog.example.goallog.domain.user.dto.LoginRequest;
import goalLog.example.goallog.domain.user.dto.LoginResponse;
import goalLog.example.goallog.domain.user.dto.SignupRequest;
import goalLog.example.goallog.domain.user.service.AuthService;
import goalLog.example.goallog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}