package goalLog.example.goallog.domain.goal.repository;

import goalLog.example.goallog.domain.goal.entity.LongTermGoal;
import goalLog.example.goallog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LongTermGoalRepository extends JpaRepository<LongTermGoal, Long> {

    // 특정 유저의 목표 전체 조회
    List<LongTermGoal> findByUser(User user);
}