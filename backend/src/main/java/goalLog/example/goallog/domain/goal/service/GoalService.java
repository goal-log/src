package goalLog.example.goallog.domain.goal.service;

import goalLog.example.goallog.domain.goal.dto.*;
import goalLog.example.goallog.domain.goal.entity.LongTermGoal;
import goalLog.example.goallog.domain.goal.repository.LongTermGoalRepository;
import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import goalLog.example.goallog.domain.plan.repository.DailyPlanRepository;
import goalLog.example.goallog.domain.task.repository.TaskRepository;
import goalLog.example.goallog.domain.user.entity.User;
import goalLog.example.goallog.domain.user.repository.UserRepository;
import goalLog.example.goallog.global.exception.CustomException;
import goalLog.example.goallog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final LongTermGoalRepository goalRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // 목표 생성
    @Transactional
    public GoalResponse create(String email, GoalCreateRequest request) {
        User user = getUser(email);

        LongTermGoal goal = LongTermGoal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .build();

        return new GoalResponse(goalRepository.save(goal));
    }

    // 내 목표 전체 조회
    @Transactional(readOnly = true)
    public List<GoalResponse> getAll(String email) {
        User user = getUser(email);
        return goalRepository.findByUser(user).stream()
                .map(GoalResponse::new)
                .toList();
    }

    // 목표 단건 조회
    @Transactional(readOnly = true)
    public GoalResponse getOne(String email, Long goalId) {
        LongTermGoal goal = getGoal(email, goalId);
        return new GoalResponse(goal);
    }

    // 목표 수정
    @Transactional
    public GoalResponse update(String email, Long goalId, GoalUpdateRequest request) {
        LongTermGoal goal = getGoal(email, goalId);
        goal.update(request.getTitle(), request.getDescription(), request.getDeadline());
        return new GoalResponse(goal);
    }

    // 목표 삭제
    @Transactional
    public void delete(String email, Long goalId) {
        LongTermGoal goal = getGoal(email, goalId);
        goalRepository.delete(goal);
    }

    // 목표 진행률 조회
    @Transactional(readOnly = true)
    public GoalProgressResponse getProgress(String email, Long goalId) {
        getGoal(email, goalId); // 본인 목표인지 확인

        List<DailyPlan> plans = dailyPlanRepository.findByLongTermGoalId(goalId);

        long totalTasks = 0;
        long completedTasks = 0;

        for (DailyPlan plan : plans) {
            List<?> tasks = taskRepository.findByDailyPlan(plan);
            totalTasks += tasks.size();
            completedTasks += taskRepository.countByDailyPlanAndCompletedTrue(plan);
        }

        int percent = totalTasks == 0 ? 0 : (int) (completedTasks * 100 / totalTasks);
        return new GoalProgressResponse(totalTasks, completedTasks, percent);
    }

    // 목표 조회 + 본인 소유 확인
    private LongTermGoal getGoal(String email, Long goalId) {
        LongTermGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));

        if (!goal.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        return goal;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}