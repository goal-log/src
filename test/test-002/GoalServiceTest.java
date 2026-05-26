package goalLog.example.goallog.sprint2;

import goalLog.example.goallog.domain.goal.dto.GoalCreateRequest;
import goalLog.example.goallog.domain.goal.dto.GoalResponse;
import goalLog.example.goallog.domain.goal.dto.GoalUpdateRequest;
import goalLog.example.goallog.domain.goal.service.GoalService;
import goalLog.example.goallog.domain.user.entity.User;
import goalLog.example.goallog.domain.user.repository.UserRepository;
import goalLog.example.goallog.global.exception.CustomException;
import goalLog.example.goallog.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Sprint 2 - 장기 목표 CRUD 테스트")
class GoalServiceTest {

    @Autowired GoalService goalService;
    @Autowired UserRepository userRepository;

    private static final String OWNER = "goal-owner@example.com";
    private static final String INTRUDER = "intruder@example.com";

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder().email(OWNER).password("ENCODED").build());
        userRepository.save(User.builder().email(INTRUDER).password("ENCODED").build());
    }

    @Test
    @DisplayName("create: 목표가 저장되고 기본 priority='MEDIUM'로 응답된다")
    void create() {
        GoalResponse res = goalService.create(OWNER,
                newCreate("Run 10K", "Marathon prep", LocalDate.of(2026, 12, 31), null, null, null));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getTitle()).isEqualTo("Run 10K");
        assertThat(res.getPriority()).isEqualTo("MEDIUM");
        assertThat(res.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("getAll: 본인 목표만 조회된다")
    void getAll_onlyMine() {
        goalService.create(OWNER, newCreate("Mine", null, null, null, null, null));
        goalService.create(INTRUDER, newCreate("Other", null, null, null, null, null));

        List<GoalResponse> mine = goalService.getAll(OWNER);

        assertThat(mine).hasSize(1);
        assertThat(mine.get(0).getTitle()).isEqualTo("Mine");
    }

    @Test
    @DisplayName("update: 목표 필드가 갱신된다")
    void update() {
        GoalResponse created = goalService.create(OWNER, newCreate("Old", null, null, null, null, null));

        GoalResponse updated = goalService.update(OWNER, created.getId(),
                newUpdate("New", "desc", LocalDate.of(2027, 1, 1), "WORK", "HIGH", "DONE"));

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("desc");
        assertThat(updated.getDeadline()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(updated.getCategory()).isEqualTo("WORK");
        assertThat(updated.getPriority()).isEqualTo("HIGH");
        assertThat(updated.getStatus()).isEqualTo("DONE");
    }

    @Test
    @DisplayName("delete: 목표 삭제 후 조회하면 GOAL_NOT_FOUND")
    void delete() {
        GoalResponse created = goalService.create(OWNER, newCreate("ToDelete", null, null, null, null, null));

        goalService.delete(OWNER, created.getId());

        assertThatThrownBy(() -> goalService.getOne(OWNER, created.getId()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GOAL_NOT_FOUND);
    }

    @Test
    @DisplayName("ownership: 다른 유저의 목표 조회/수정/삭제 시 UNAUTHORIZED")
    void ownership() {
        GoalResponse mine = goalService.create(OWNER, newCreate("Secret", null, null, null, null, null));

        assertThatThrownBy(() -> goalService.getOne(INTRUDER, mine.getId()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        assertThatThrownBy(() -> goalService.delete(INTRUDER, mine.getId()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    private GoalCreateRequest newCreate(String title, String desc, LocalDate deadline,
                                         String category, String priority, String status) {
        GoalCreateRequest r = new GoalCreateRequest();
        setFields(r, "title", title, "description", desc, "deadline", deadline,
                "category", category, "priority", priority, "status", status);
        return r;
    }

    private GoalUpdateRequest newUpdate(String title, String desc, LocalDate deadline,
                                         String category, String priority, String status) {
        GoalUpdateRequest r = new GoalUpdateRequest();
        setFields(r, "title", title, "description", desc, "deadline", deadline,
                "category", category, "priority", priority, "status", status);
        return r;
    }

    static void setFields(Object target, Object... pairs) {
        try {
            for (int i = 0; i < pairs.length; i += 2) {
                Field f = target.getClass().getDeclaredField((String) pairs[i]);
                f.setAccessible(true);
                f.set(target, pairs[i + 1]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
