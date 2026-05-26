package goalLog.example.goallog.sprint3;

import goalLog.example.goallog.domain.goal.dto.GoalCreateRequest;
import goalLog.example.goallog.domain.goal.dto.GoalProgressResponse;
import goalLog.example.goallog.domain.goal.dto.GoalResponse;
import goalLog.example.goallog.domain.goal.service.GoalService;
import goalLog.example.goallog.domain.plan.dto.PlanCreateRequest;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
import goalLog.example.goallog.domain.plan.service.PlanService;
import goalLog.example.goallog.domain.task.dto.TaskCreateRequest;
import goalLog.example.goallog.domain.task.service.TaskService;
import goalLog.example.goallog.domain.user.entity.User;
import goalLog.example.goallog.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Sprint 3 - 진행률 계산 테스트")
class GoalProgressTest {

    @Autowired GoalService goalService;
    @Autowired PlanService planService;
    @Autowired TaskService taskService;
    @Autowired UserRepository userRepository;

    private static final String OWNER = "progress@example.com";

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder().email(OWNER).password("ENCODED").build());
    }

    @Test
    @DisplayName("Task가 없으면 진행률은 0%, total/completed 모두 0")
    void noTasks() {
        GoalResponse goal = createGoal();

        GoalProgressResponse progress = goalService.getProgress(OWNER, goal.getId());

        assertThat(progress.getTotalTasks()).isZero();
        assertThat(progress.getCompletedTasks()).isZero();
        assertThat(progress.getProgressPercent()).isZero();
    }

    @Test
    @DisplayName("Task 4개 중 1개 완료 → 25%")
    void quarter() {
        GoalResponse goal = createGoal();
        PlanResponse plan = createPlanFor(goal.getId());
        addTask(plan.getId(), "A");
        addTask(plan.getId(), "B");
        addTask(plan.getId(), "C");
        Long d = addTask(plan.getId(), "D");

        taskService.toggle(OWNER, d);

        GoalProgressResponse progress = goalService.getProgress(OWNER, goal.getId());

        assertThat(progress.getTotalTasks()).isEqualTo(4);
        assertThat(progress.getCompletedTasks()).isEqualTo(1);
        assertThat(progress.getProgressPercent()).isEqualTo(25);
    }

    @Test
    @DisplayName("Task 3개 모두 완료 → 100%")
    void allDone() {
        GoalResponse goal = createGoal();
        PlanResponse plan = createPlanFor(goal.getId());
        Long a = addTask(plan.getId(), "A");
        Long b = addTask(plan.getId(), "B");
        Long c = addTask(plan.getId(), "C");

        taskService.toggle(OWNER, a);
        taskService.toggle(OWNER, b);
        taskService.toggle(OWNER, c);

        GoalProgressResponse progress = goalService.getProgress(OWNER, goal.getId());

        assertThat(progress.getProgressPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("여러 플랜에 걸친 Task를 합산해 진행률을 계산한다")
    void multiplePlans() {
        GoalResponse goal = createGoal();
        PlanResponse p1 = createPlanFor(goal.getId());
        PlanResponse p2 = createPlanForDate(goal.getId(), LocalDate.of(2026, 5, 27));

        Long t1 = addTask(p1.getId(), "p1-a");
        addTask(p1.getId(), "p1-b");
        addTask(p2.getId(), "p2-a");
        addTask(p2.getId(), "p2-b");

        taskService.toggle(OWNER, t1);

        GoalProgressResponse progress = goalService.getProgress(OWNER, goal.getId());

        assertThat(progress.getTotalTasks()).isEqualTo(4);
        assertThat(progress.getCompletedTasks()).isEqualTo(1);
        assertThat(progress.getProgressPercent()).isEqualTo(25);
    }

    @Test
    @DisplayName("Task 3개 중 1개 완료 → 33% (정수 절삭)")
    void truncation() {
        GoalResponse goal = createGoal();
        PlanResponse plan = createPlanFor(goal.getId());
        Long a = addTask(plan.getId(), "A");
        addTask(plan.getId(), "B");
        addTask(plan.getId(), "C");

        taskService.toggle(OWNER, a);

        GoalProgressResponse progress = goalService.getProgress(OWNER, goal.getId());

        assertThat(progress.getProgressPercent()).isEqualTo(33);
    }

    private GoalResponse createGoal() {
        GoalCreateRequest r = new GoalCreateRequest();
        setField(r, "title", "Progress Goal");
        return goalService.create(OWNER, r);
    }

    private PlanResponse createPlanFor(Long goalId) {
        return createPlanForDate(goalId, LocalDate.of(2026, 5, 26));
    }

    private PlanResponse createPlanForDate(Long goalId, LocalDate date) {
        PlanCreateRequest r = new PlanCreateRequest();
        setField(r, "date", date);
        setField(r, "longTermGoalId", goalId);
        return planService.create(OWNER, r);
    }

    private Long addTask(Long planId, String title) {
        TaskCreateRequest r = new TaskCreateRequest();
        setField(r, "title", title);
        return planService.addTask(OWNER, planId, r).getId();
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
