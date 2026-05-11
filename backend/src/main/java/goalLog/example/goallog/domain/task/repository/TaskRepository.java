package goalLog.example.goallog.domain.task.repository;

import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import goalLog.example.goallog.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 특정 플랜에 속한 태스크 전체 조회
    List<Task> findByDailyPlan(DailyPlan dailyPlan);

    // 완료된 태스크 수 조회 - 진행률 계산에 사용
    long countByDailyPlanAndCompletedTrue(DailyPlan dailyPlan);
}