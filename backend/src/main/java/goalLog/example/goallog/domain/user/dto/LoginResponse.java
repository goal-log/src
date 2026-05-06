package goalLog.example.goallog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    // 로그인 성공 시 클라이언트에 토큰만 내려줌
    private String token;
}