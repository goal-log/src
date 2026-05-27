package goalLog.example.goallog.sprint3;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 3 시나리오 E2E: signup → login → goal → plan → tasks → toggle → progress
 * 백엔드 전체 흐름을 HTTP 레벨에서 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sprint 3 - 전체 플로우 E2E 시나리오")
class EndToEndScenarioIT {

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
    @DisplayName("회원가입 → 로그인 → 목표/플랜/Task 생성 → 토글 → 진행률 50% 검증")
    void fullFlow() throws Exception {
        // 1) signup
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "e2e@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk());

        // 2) login → JWT
        MvcResult loginRes = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "e2e@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk())
                .andReturn();
        String token = readJson(loginRes).at("/data/token").asText();
        assertThat(token).isNotBlank();
        String bearer = "Bearer " + token;

        // 3) create goal
        MvcResult goalRes = mvc.perform(post("/api/goals")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", "Marathon"))))
                .andExpect(status().isOk())
                .andReturn();
        long goalId = readJson(goalRes).at("/data/id").asLong();

        // 4) create plan tied to goal
        MvcResult planRes = mvc.perform(post("/api/plans")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "date", "2026-05-26",
                                "longTermGoalId", goalId))))
                .andExpect(status().isOk())
                .andReturn();
        long planId = readJson(planRes).at("/data/id").asLong();

        // 5) add 2 tasks
        long t1 = createTask(bearer, planId, "Run 5km");
        long t2 = createTask(bearer, planId, "Cool down");

        // 6) toggle one task
        mvc.perform(patch("/api/tasks/" + t1 + "/toggle")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        // 7) progress should be 50%
        mvc.perform(get("/api/goals/" + goalId + "/progress")
                        .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTasks").value(2))
                .andExpect(jsonPath("$.data.completedTasks").value(1))
                .andExpect(jsonPath("$.data.progressPercent").value(50));

        // 8) toggle second → 100%
        mvc.perform(patch("/api/tasks/" + t2 + "/toggle")
                        .header("Authorization", bearer))
                .andExpect(status().isOk());

        mvc.perform(get("/api/goals/" + goalId + "/progress")
                        .header("Authorization", bearer))
                .andExpect(jsonPath("$.data.progressPercent").value(100));
    }

    @Test
    @DisplayName("토큰 없이 보호된 API 호출 시 401/403 으로 차단된다")
    void unauthorized_withoutToken() throws Exception {
        mvc.perform(get("/api/goals"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertThat(s).isIn(401, 403);
                });
    }

    private long createTask(String bearer, long planId, String title) throws Exception {
        MvcResult res = mvc.perform(post("/api/plans/" + planId + "/tasks")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", title))))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(res).at("/data/id").asLong();
    }

    private JsonNode readJson(MvcResult res) throws Exception {
        return om.readTree(res.getResponse().getContentAsByteArray());
    }
}
