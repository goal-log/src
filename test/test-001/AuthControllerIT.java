package goalLog.example.goallog.sprint1;

import goalLog.example.goallog.domain.user.repository.UserRepository;
import goalLog.example.goallog.support.TestDbCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sprint 1 - 회원가입/로그인 API 통합 테스트")
class AuthControllerIT {

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;
    @Autowired TestDbCleaner dbCleaner;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        dbCleaner.cleanAll();
    }

    @Test
    @DisplayName("POST /api/auth/signup: 신규 가입 → success=true")
    void signup_success() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("alice@example.com", "pw1234!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/signup: 중복 이메일 → 409 + success=false")
    void signup_duplicate() throws Exception {
        String body = json("dup@example.com", "pw1234!");
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login: 가입한 계정으로 로그인 → 200 + JWT 발급")
    void login_success() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("bob@example.com", "pw1234!")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("bob@example.com", "pw1234!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/login: 잘못된 비밀번호 → 401 INVALID_PASSWORD")
    void login_wrongPassword() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("carol@example.com", "pw1234!")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("carol@example.com", "WRONG_PW")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login: 미가입 이메일 → 404 USER_NOT_FOUND")
    void login_notFound() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("ghost@example.com", "pw1234!")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private String json(String email, String password) throws Exception {
        return om.writeValueAsString(Map.of("email", email, "password", password));
    }
}
