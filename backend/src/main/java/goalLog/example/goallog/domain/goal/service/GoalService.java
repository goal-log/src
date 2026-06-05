package goalLog.example.goallog.domain.goal.service;

import goalLog.example.goallog.domain.goal.dto.*;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
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

// [GRASP: High Cohesion] - 장기 목표 도메인에 관련된 기능(CRUD, 진행률, 플랜 조회)만 담당한다.
// [GRASP: Low Coupling] - UserRepository, GoalRepository 등 필요한 의존성만 주입받아 사용한다.
@Service
@RequiredArgsConstructor
public class GoalService {

    private final LongTermGoalRepository goalRepository;
    private final DailyPlanRepository dailyPlanRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // [GRASP: Creator] - GoalService가 LongTermGoal 생성에 필요한 User 정보를 알고 있으므로 생성 책임을 갖는다.
    @Transactional
    public GoalResponse create(String email, GoalCreateRequest request) {
        User user = getUser(email);

        LongTermGoal goal = LongTermGoal.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(request.getStatus())
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
        goal.update(request.getTitle(), request.getDescription(), request.getDeadline(),
                request.getCategory(), request.getPriority(), request.getStatus());
        return new GoalResponse(goal);
    }

    // 목표 삭제
    @Transactional
    public void delete(String email, Long goalId) {
        LongTermGoal goal = getGoal(email, goalId);
        List<DailyPlan> plans = dailyPlanRepository.findByLongTermGoalId(goalId);
        for (DailyPlan plan : plans) {
            taskRepository.deleteAll(taskRepository.findByDailyPlan(plan));
        }
        dailyPlanRepository.deleteAll(plans);
        goalRepository.delete(goal);
    }

    // [GRASP: Information Expert] - 목표 진행률 계산에 필요한 플랜·태스크 정보를 가장 잘 알고 있으므로 이 책임을 맡는다.
    @Transactional(readOnly = true)
    public GoalProgressResponse getProgress(String email, Long goalId) {
        LongTermGoal goal = getGoal(email, goalId);

        List<DailyPlan> plans = dailyPlanRepository.findByLongTermGoalId(goalId);

        long totalTasks = 0;
        long completedTasks = 0;

        for (DailyPlan plan : plans) {
            List<?> tasks = taskRepository.findByDailyPlan(plan);
            totalTasks += tasks.size();
            completedTasks += taskRepository.countByDailyPlanAndCompletedTrue(plan);
        }

        int percent;
        if (goal.getStatus().equals("COMPLETED")) {
            percent = 100;
        } else {
            percent = totalTasks == 0 ? 0 : (int) (completedTasks * 100 / totalTasks);
        }
        return new GoalProgressResponse(totalTasks, completedTasks, percent);
    }

    // 목표에 연결된 플랜 목록 조회
    @Transactional(readOnly = true)
    public List<PlanResponse> getPlans(String email, Long goalId) {
        getGoal(email, goalId); // 본인 목표인지 확인
        return dailyPlanRepository.findByLongTermGoalId(goalId).stream()
                .map(PlanResponse::new)
                .toList();
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