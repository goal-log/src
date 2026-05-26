package goalLog.example.goallog.sprint2;

import goalLog.example.goallog.domain.goal.dto.GoalCreateRequest;
import goalLog.example.goallog.domain.goal.dto.GoalResponse;
import goalLog.example.goallog.domain.goal.service.GoalService;
import goalLog.example.goallog.domain.plan.dto.PlanCreateRequest;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
import goalLog.example.goallog.domain.plan.service.PlanService;
import goalLog.example.goallog.domain.task.dto.TaskCreateRequest;
import goalLog.example.goallog.domain.task.dto.TaskResponse;
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

import java.time.LocalDate;
import java.util.List;

import static goalLog.example.goallog.sprint2.GoalServiceTest.setFields;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Sprint 2 - 하루 계획 CRUD 테스트")
class PlanServiceTest {

    @Autowired PlanService planService;
    @Autowired GoalService goalService;
    @Autowired UserRepository userRepository;

    private static final String OWNER = "plan-owner@example.com";
    private static final String INTRUDER = "plan-intruder@example.com";

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder().email(OWNER).password("ENCODED").build());
        userRepository.save(User.builder().email(INTRUDER).password("ENCODED").build());
    }

    @Test
    @DisplayName("create: 목표 없이 플랜만 생성할 수 있다 (longTermGoalId=null)")
    void create_withoutGoal() {
        PlanResponse plan = planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), null));

        assertThat(plan.getId()).isNotNull();
        assertThat(plan.getDate()).isEqualTo(LocalDate.of(2026, 5, 26));
        assertThat(plan.getLongTermGoalId()).isNull();
        assertThat(plan.getTasks()).isEmpty();
    }

    @Test
    @DisplayName("create: 장기 목표와 연결해서 플랜을 생성할 수 있다")
    void create_withGoal() {
        GoalResponse goal = goalService.create(OWNER, newGoal("Goal A"));

        PlanResponse plan = planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), goal.getId()));

        assertThat(plan.getLongTermGoalId()).isEqualTo(goal.getId());
    }

    @Test
    @DisplayName("create: 존재하지 않는 longTermGoalId 지정 시 GOAL_NOT_FOUND")
    void create_goalNotFound() {
        assertThatThrownBy(() -> planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), 9999L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GOAL_NOT_FOUND);
    }

    @Test
    @DisplayName("getByDate: 해당 날짜의 본인 플랜만 조회된다")
    void getByDate() {
        planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), null));
        planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 27), null));
        planService.create(INTRUDER, newPlan(LocalDate.of(2026, 5, 26), null));

        List<PlanResponse> plans = planService.getByDate(OWNER, LocalDate.of(2026, 5, 26));

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 26));
    }

    @Test
    @DisplayName("addTask: 플랜에 Task를 추가하면 completed=false로 저장된다")
    void addTask() {
        PlanResponse plan = planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), null));

        TaskResponse task = planService.addTask(OWNER, plan.getId(), newTask("Run 5km"));

        assertThat(task.getId()).isNotNull();
        assertThat(task.getTitle()).isEqualTo("Run 5km");
        assertThat(task.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("delete: 본인 플랜 삭제 후 조회하면 PLAN_NOT_FOUND")
    void delete() {
        PlanResponse plan = planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), null));

        planService.delete(OWNER, plan.getId());

        assertThatThrownBy(() -> planService.getById(OWNER, plan.getId()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PLAN_NOT_FOUND);
    }

    @Test
    @DisplayName("ownership: 다른 유저의 플랜 접근 시 UNAUTHORIZED")
    void ownership() {
        PlanResponse plan = planService.create(OWNER, newPlan(LocalDate.of(2026, 5, 26), null));

        assertThatThrownBy(() -> planService.getById(INTRUDER, plan.getId()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        assertThatThrownBy(() -> planService.addTask(INTRUDER, plan.getId(), newTask("evil")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    private PlanCreateRequest newPlan(LocalDate date, Long goalId) {
        PlanCreateRequest r = new PlanCreateRequest();
        setFields(r, "date", date, "longTermGoalId", goalId);
        return r;
    }

    private GoalCreateRequest newGoal(String title) {
        GoalCreateRequest r = new GoalCreateRequest();
        setFields(r, "title", title);
        return r;
    }

    private TaskCreateRequest newTask(String title) {
        TaskCreateRequest r = new TaskCreateRequest();
        setFields(r, "title", title);
        return r;
    }
}
