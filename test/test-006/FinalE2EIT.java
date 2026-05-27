package goalLog.example.goallog.sprint6;

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
 * Sprint 6 — 최종 E2E 시나리오.
 * 두 명의 사용자가 각자 목표/플랜/Task 를 관리하며,
 * 서로의 리소스에 접근할 수 없고, 진행률이 정확히 갱신되는지
 * 종합적으로 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Sprint 6 - 최종 E2E 통합 시나리오")
class FinalE2EIT {

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
    @DisplayName("다중 사용자 시나리오: 격리, CRUD, 진행률 변화 전체 검증")
    void multiUserScenario() throws Exception {
        // === 사용자 A 가입/로그인 ===
        String tokenA = signupAndLogin("a@example.com", "pwA1234!");
        // === 사용자 B 가입/로그인 ===
        String tokenB = signupAndLogin("b@example.com", "pwB1234!");

        // A: 목표 + 플랜 + Task 3개 생성
        long goalA = createGoal(tokenA, "A's marathon");
        long planA = createPlan(tokenA, "2026-05-26", goalA);
        long taskA1 = createTask(tokenA, planA, "Run 5km");
        long taskA2 = createTask(tokenA, planA, "Stretch");
        long taskA3 = createTask(tokenA, planA, "Cool down");

        // B: 목표 + 플랜 + Task 2개 생성
        long goalB = createGoal(tokenB, "B's reading");
        long planB = createPlan(tokenB, "2026-05-26", goalB);
        long taskB1 = createTask(tokenB, planB, "Read 30 pages");
        long taskB2 = createTask(tokenB, planB, "Notes");

        // 격리 검증: A가 B의 목표 조회 시 UNAUTHORIZED
        mvc.perform(get("/api/goals/" + goalB).header("Authorization", tokenA))
                .andExpect(status().isUnauthorized());
        // A가 B의 Task 토글 시 UNAUTHORIZED
        mvc.perform(patch("/api/tasks/" + taskB1 + "/toggle").header("Authorization", tokenA))
                .andExpect(status().isUnauthorized());

        // A: 1개 완료 → 33%
        mvc.perform(patch("/api/tasks/" + taskA1 + "/toggle").header("Authorization", tokenA))
                .andExpect(status().isOk());
        mvc.perform(get("/api/goals/" + goalA + "/progress").header("Authorization", tokenA))
                .andExpect(jsonPath("$.data.totalTasks").value(3))
                .andExpect(jsonPath("$.data.completedTasks").value(1))
                .andExpect(jsonPath("$.data.progressPercent").value(33));

        // A: 두 번째 완료 → 66%
        mvc.perform(patch("/api/tasks/" + taskA2 + "/toggle").header("Authorization", tokenA))
                .andExpect(status().isOk());
        mvc.perform(get("/api/goals/" + goalA + "/progress").header("Authorization", tokenA))
                .andExpect(jsonPath("$.data.progressPercent").value(66));

        // A: 세 번째 완료 → 100%
        mvc.perform(patch("/api/tasks/" + taskA3 + "/toggle").header("Authorization", tokenA))
                .andExpect(status().isOk());
        mvc.perform(get("/api/goals/" + goalA + "/progress").header("Authorization", tokenA))
                .andExpect(jsonPath("$.data.progressPercent").value(100));

        // A: 첫 Task 다시 토글 (해제) → 66%
        mvc.perform(patch("/api/tasks/" + taskA1 + "/toggle").header("Authorization", tokenA))
                .andExpect(status().isOk());
        mvc.perform(get("/api/goals/" + goalA + "/progress").header("Authorization", tokenA))
                .andExpect(jsonPath("$.data.progressPercent").value(66));

        // A: Task 삭제 → 진행률 재계산
        mvc.perform(delete("/api/tasks/" + taskA1).header("Authorization", tokenA))
                .andExpect(status().isOk());
        mvc.perform(get("/api/goals/" + goalA + "/progress").header("Authorization", tokenA))
                .andExpect(jsonPath("$.data.totalTasks").value(2))
                .andExpect(jsonPath("$.data.completedTasks").value(2))
                .andExpect(jsonPath("$.data.progressPercent").value(100));

        // B는 영향 없음: 아직 0%
        mvc.perform(get("/api/goals/" + goalB + "/progress").header("Authorization", tokenB))
                .andExpect(jsonPath("$.data.progressPercent").value(0));

        // B: 본인 Task 토글 → 50%
        mvc.perform(patch("/api/tasks/" + taskB1 + "/toggle").header("Authorization", tokenB))
                .andExpect(status().isOk());
        mvc.perform(get("/api/goals/" + goalB + "/progress").header("Authorization", tokenB))
                .andExpect(jsonPath("$.data.progressPercent").value(50));

        // 목표 목록: A는 1개, B는 1개
        mvc.perform(get("/api/goals").header("Authorization", tokenA))
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(get("/api/goals").header("Authorization", tokenB))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 보호된 API 호출 시 401/403 차단")
    void invalidToken() throws Exception {
        mvc.perform(get("/api/goals").header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertThat(s).isIn(401, 403);
                });
    }

    // ---- helpers ----

    private String signupAndLogin(String email, String password) throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk());
        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        return "Bearer " + readJson(res).at("/data/token").asText();
    }

    private long createGoal(String token, String title) throws Exception {
        MvcResult res = mvc.perform(post("/api/goals")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", title))))
                .andExpect(status().isOk()).andReturn();
        return readJson(res).at("/data/id").asLong();
    }

    private long createPlan(String token, String date, long goalId) throws Exception {
        MvcResult res = mvc.perform(post("/api/plans")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("date", date, "longTermGoalId", goalId))))
                .andExpect(status().isOk()).andReturn();
        return readJson(res).at("/data/id").asLong();
    }

    private long createTask(String token, long planId, String title) throws Exception {
        MvcResult res = mvc.perform(post("/api/plans/" + planId + "/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", title))))
                .andExpect(status().isOk()).andReturn();
        return readJson(res).at("/data/id").asLong();
    }

    private JsonNode readJson(MvcResult res) throws Exception {
        return om.readTree(res.getResponse().getContentAsByteArray());
    }
}
