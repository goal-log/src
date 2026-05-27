package goalLog.example.goallog.support;

import goalLog.example.goallog.domain.goal.repository.LongTermGoalRepository;
import goalLog.example.goallog.domain.plan.repository.DailyPlanRepository;
import goalLog.example.goallog.domain.task.repository.TaskRepository;
import goalLog.example.goallog.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FK 제약을 지키는 순서로 모든 도메인 테이블을 비운다.
 * 통합 테스트가 다른 클래스의 잔여 데이터로 깨지지 않도록 사용한다.
 */
@Component
public class TestDbCleaner {

    private final TaskRepository taskRepository;
    private final DailyPlanRepository planRepository;
    private final LongTermGoalRepository goalRepository;
    private final UserRepository userRepository;

    public TestDbCleaner(TaskRepository taskRepository,
                         DailyPlanRepository planRepository,
                         LongTermGoalRepository goalRepository,
                         UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.planRepository = planRepository;
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void cleanAll() {
        taskRepository.deleteAllInBatch();
        planRepository.deleteAllInBatch();
        goalRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
