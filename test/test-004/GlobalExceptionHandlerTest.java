package goalLog.example.goallog.sprint4;

import goalLog.example.goallog.global.common.ApiResponse;
import goalLog.example.goallog.global.exception.CustomException;
import goalLog.example.goallog.global.exception.ErrorCode;
import goalLog.example.goallog.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sprint 4 - GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("CustomException → ErrorCode 의 HTTP 상태와 메시지로 응답")
    void customException() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleCustomException(new CustomException(ErrorCode.GOAL_NOT_FOUND));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.GOAL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("EMAIL_DUPLICATE → 409 Conflict")
    void emailDuplicate() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleCustomException(new CustomException(ErrorCode.EMAIL_DUPLICATE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("INVALID_PASSWORD / UNAUTHORIZED → 401 Unauthorized")
    void unauthorizedFamily() {
        assertThat(handler.handleCustomException(new CustomException(ErrorCode.INVALID_PASSWORD)).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.handleCustomException(new CustomException(ErrorCode.UNAUTHORIZED)).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("예상 못한 Exception → 500 + 일반 메시지")
    void unexpectedException() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleException(new IllegalStateException("DB connection lost"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("서버 오류가 발생했습니다.");
    }
}
