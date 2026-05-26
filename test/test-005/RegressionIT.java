package goalLog.example.goallog.sprint5;

import goalLog.example.goallog.domain.user.repository.UserRepository;
import goalLog.example.goallog.support.TestDbCleaner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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
 * Sprint 5 — 회귀 테스트.
 * 리팩터링(GRASP 패턴 주석 추가 등) 이후에도 핵심 사용자 시나리오가 깨지지 않았는지
 * 빠르게 확인하기 위한 단일 흐름 스모크 테스트.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Sprint 5 - 회귀 스모크 테스트 (리팩터링 후 핵심 흐름 보존)")
class RegressionIT {

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;
    @Autowired TestDbCleaner dbCleaner;

    MockMvc mvc;
    private String token;
    private long goalId;
    private long planId;
    private long taskId;

    @BeforeAll
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        dbCleaner.cleanAll();
    }

    @AfterAll
    void tearDown() {
        dbCleaner.cleanAll();
    }

    @Test
    @Order(1)
    @DisplayName("회원가입 + 로그인 흐름")
    void step01_auth() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "reg@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk());

        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "reg@example.com", "password", "pw1234!"))))
                .andExpect(status().isOk()).andReturn();
        token = "Bearer " + readJson(res).at("/data/token").asText();
        assertThat(token).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("목표/플랜/Task CRUD가 동작한다")
    void step02_crud() throws Exception {
        MvcResult g = mvc.perform(post("/api/goals")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", "Regress"))))
                .andExpect(status().isOk()).andReturn();
        goalId = readJson(g).at("/data/id").asLong();

        MvcResult p = mvc.perform(post("/api/plans")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("date", "2026-05-26", "longTermGoalId", goalId))))
                .andExpect(status().isOk()).andReturn();
        planId = readJson(p).at("/data/id").asLong();

        MvcResult t = mvc.perform(post("/api/plans/" + planId + "/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("title", "Run"))))
                .andExpect(status().isOk()).andReturn();
        taskId = readJson(t).at("/data/id").asLong();
    }

    @Test
    @Order(3)
    @DisplayName("Task 토글 → 진행률 100%")
    void step03_progress() throws Exception {
        mvc.perform(patch("/api/tasks/" + taskId + "/toggle")
                        .header("Authorization", token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/goals/" + goalId + "/progress")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercent").value(100));
    }

    @Test
    @Order(4)
    @DisplayName("리소스 정리: Task → Plan 순으로 삭제하면 GOAL은 유지된다")
    void step04_cleanup() throws Exception {
        // 자식 → 부모 순서로 삭제 (FK 제약을 따름)
        mvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/plans/" + planId)
                        .header("Authorization", token))
                .andExpect(status().isOk());

        mvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private JsonNode readJson(MvcResult res) throws Exception {
        return om.readTree(res.getResponse().getContentAsByteArray());
    }
}
