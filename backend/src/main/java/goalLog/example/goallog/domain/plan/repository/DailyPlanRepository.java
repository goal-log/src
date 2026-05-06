package goalLog.example.goallog.domain.plan.repository;

import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import goalLog.example.goallog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {

    // 특정 유저의 특정 날짜 플랜 조회
    Optional<DailyPlan> findByUserAndDate(User user, LocalDate date);

    // 특정 유저의 플랜 전체 조회
    List<DailyPlan> findByUser(User user);

    // 특정 장기 목표에 연결된 플랜 전체 조회 - 진행률 계산에 사용
    List<DailyPlan> findByLongTermGoalId(Long longTermGoalId);
}