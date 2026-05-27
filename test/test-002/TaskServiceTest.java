package goalLog.example.goallog.sprint2;

import goalLog.example.goallog.domain.plan.dto.PlanCreateRequest;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
import goalLog.example.goallog.domain.plan.service.PlanService;
import goalLog.example.goallog.domain.task.dto.TaskCreateRequest;
import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.domain.task.repository.TaskRepository;
import goalLog.example.goallog.domain.task.service.TaskService;
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

import static goalLog.example.goallog.sprint2.GoalServiceTest.setFields;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Sprint 2 - Task 완료 토글 / 삭제 테스트")
class TaskServiceTest {

    @Autowired TaskService taskService;
    @Autowired PlanService planService;
    @Autowired UserRepository userRepository;
    @Autowired TaskRepository taskRepository;

    private static final String OWNER = "task-owner@example.com";
    private static final String INTRUDER = "task-intruder@example.com";

    private Long taskId;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder().email(OWNER).password("ENCODED").build());
        userRepository.save(User.builder().email(INTRUDER).password("ENCODED").build());

        PlanCreateRequest p = new PlanCreateRequest();
        setFields(p, "date", LocalDate.of(2026, 5, 26), "longTermGoalId", null);
        PlanResponse plan = planService.create(OWNER, p);

        TaskCreateRequest t = new TaskCreateRequest();
        setFields(t, "title", "Initial task");
        taskId = planService.addTask(OWNER, plan.getId(), t).getId();
    }

    @Test
    @DisplayName("toggle: false → true → false 로 완료 상태가 반전된다")
    void toggle() {
        TaskResponse first = taskService.toggle(OWNER, taskId);
        assertThat(first.isCompleted()).isTrue();

        TaskResponse second = taskService.toggle(OWNER, taskId);
        assertThat(second.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("delete: 본인 Task 삭제 시 저장소에서 제거된다")
    void delete() {
        taskService.delete(OWNER, taskId);

        assertThat(taskRepository.findById(taskId)).isEmpty();
    }

    @Test
    @DisplayName("ownership: 다른 유저의 Task 토글/삭제 시 UNAUTHORIZED")
    void ownership() {
        assertThatThrownBy(() -> taskService.toggle(INTRUDER, taskId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        assertThatThrownBy(() -> taskService.delete(INTRUDER, taskId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("toggle: 존재하지 않는 ID → TASK_NOT_FOUND")
    void notFound() {
        assertThatThrownBy(() -> taskService.toggle(OWNER, 99999L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TASK_NOT_FOUND);
    }
}
