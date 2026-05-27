package goalLog.example.goallog.sprint1;

import goalLog.example.goallog.domain.user.dto.LoginRequest;
import goalLog.example.goallog.domain.user.dto.LoginResponse;
import goalLog.example.goallog.domain.user.dto.SignupRequest;
import goalLog.example.goallog.domain.user.entity.User;
import goalLog.example.goallog.domain.user.repository.UserRepository;
import goalLog.example.goallog.domain.user.service.AuthService;
import goalLog.example.goallog.global.exception.CustomException;
import goalLog.example.goallog.global.exception.ErrorCode;
import goalLog.example.goallog.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sprint 1 - AuthService 단위 테스트")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @InjectMocks AuthService authService;

    private SignupRequest signupReq;
    private LoginRequest loginReq;

    @BeforeEach
    void setUp() {
        signupReq = build(new SignupRequest(), "email", "test@example.com", "password", "pw1234!");
        loginReq = build(new LoginRequest(), "email", "test@example.com", "password", "pw1234!");
    }

    @Test
    @DisplayName("signup: 신규 이메일이면 BCrypt 암호화 후 저장한다")
    void signup_success() {
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("pw1234!")).willReturn("ENCODED");

        authService.signup(signupReq);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("signup: 이미 존재하는 이메일이면 EMAIL_DUPLICATE 예외를 던지고 저장하지 않는다")
    void signup_duplicate() {
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(signupReq))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_DUPLICATE);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: 비밀번호 일치 시 JWT 토큰을 반환한다")
    void login_success() {
        User user = User.builder().email("test@example.com").password("ENCODED").build();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("pw1234!", "ENCODED")).willReturn(true);
        given(jwtProvider.generateToken("test@example.com")).willReturn("jwt.token.value");

        LoginResponse response = authService.login(loginReq);

        assertThat(response.getToken()).isEqualTo("jwt.token.value");
    }

    @Test
    @DisplayName("login: 존재하지 않는 이메일이면 USER_NOT_FOUND 예외")
    void login_userNotFound() {
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("login: 비밀번호 불일치 시 INVALID_PASSWORD 예외")
    void login_wrongPassword() {
        User user = User.builder().email("test@example.com").password("ENCODED").build();
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("pw1234!", "ENCODED")).willReturn(false);

        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    private static <T> T build(T target, Object... pairs) {
        try {
            for (int i = 0; i < pairs.length; i += 2) {
                Field f = target.getClass().getDeclaredField((String) pairs[i]);
                f.setAccessible(true);
                f.set(target, pairs[i + 1]);
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
