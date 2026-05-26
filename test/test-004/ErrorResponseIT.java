package goalLog.example.goallog.sprint4;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 4 — 모든 ErrorCode가 명세대로 HTTP 상태와 응답 본문을 반환하는지 검증.
 * 공통 응답 포맷: {"success": false, "message": "..."}
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sprint 4 - 에러 응답 코드 통합 검증")
class ErrorResponseIT {

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;
    @Autowired TestDbCleaner dbCleaner;

    MockMvc mvc;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        dbCleaner.cleanAll();
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "err@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk());
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "err@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk()).andReturn();
        token = "Bearer " + om.readTree(login.getResponse().getContentAsByteArray())
                .at("/data/token").asText();
    }

    @Test
    @DisplayName("EMAIL_DUPLICATE → 409 + success=false + 메시지")
    void emailDuplicate() throws Exception {
        // 이미 가입된 이메일로 또 가입 시도
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "err@example.com", "password", "pw1234!"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("USER_NOT_FOUND → 404")
    void userNotFound() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "ghost@example.com", "password", "pw"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("INVALID_PASSWORD → 401")
    void invalidPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "err@example.com", "password", "WRONG"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GOAL_NOT_FOUND → 404")
    void goalNotFound() throws Exception {
        mvc.perform(get("/api/goals/99999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("목표를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("PLAN_NOT_FOUND → 404")
    void planNotFound() throws Exception {
        mvc.perform(delete("/api/plans/99999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("플랜을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("TASK_NOT_FOUND → 404")
    void taskNotFound() throws Exception {
        mvc.perform(patch("/api/tasks/99999/toggle")
                        .header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("태스크를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("UNAUTHORIZED — 다른 유저 리소스 접근 시 401")
    void otherUserResource() throws Exception {
        // 두 번째 유저 가입 및 로그인, 본인 목표 생성
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "other@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk());
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "other@example.com", "password", "pw1234!"))))
                .andReturn();
        String otherToken = "Bearer " + om.readTree(login.getResponse().getContentAsByteArray())
                .at("/data/token").asText();

        MvcResult goalRes = mvc.perform(post("/api/goals")
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", "Other Goal"))))
                .andExpect(status().isOk()).andReturn();
        long otherGoalId = om.readTree(goalRes.getResponse().getContentAsByteArray())
                .at("/data/id").asLong();

        // 첫 번째 유저 토큰으로 두 번째 유저 목표 조회 → UNAUTHORIZED
        mvc.perform(get("/api/goals/" + otherGoalId)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("공통 응답 포맷: 성공 시 {success, data}, 실패 시 {success, message}")
    void commonResponseShape() throws Exception {
        MvcResult success = mvc.perform(get("/api/goals")
                        .header("Authorization", token))
                .andExpect(status().isOk()).andReturn();
        JsonNode body = om.readTree(success.getResponse().getContentAsByteArray());
        assert body.has("success");
        assert body.get("success").asBoolean();
        assert body.has("data");

        MvcResult failure = mvc.perform(get("/api/goals/99999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound()).andReturn();
        JsonNode errBody = om.readTree(failure.getResponse().getContentAsByteArray());
        assert errBody.has("success");
        assert !errBody.get("success").asBoolean();
        assert errBody.has("message");
    }
}
