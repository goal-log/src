package goalLog.example.goallog.domain.plan.service;

import goalLog.example.goallog.domain.goal.entity.LongTermGoal;
import goalLog.example.goallog.domain.goal.repository.LongTermGoalRepository;
import goalLog.example.goallog.domain.plan.dto.PlanCreateRequest;
import goalLog.example.goallog.domain.plan.dto.PlanResponse;
import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import goalLog.example.goallog.domain.plan.repository.DailyPlanRepository;
import goalLog.example.goallog.domain.task.dto.TaskCreateRequest;
import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.domain.task.entity.Task;
import goalLog.example.goallog.domain.task.repository.TaskRepository;
import goalLog.example.goallog.domain.user.entity.User;
import goalLog.example.goallog.domain.user.repository.UserRepository;
import goalLog.example.goallog.global.exception.CustomException;
import goalLog.example.goallog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final DailyPlanRepository planRepository;
    private final LongTermGoalRepository goalRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlanResponse create(String email, PlanCreateRequest request) {
        User user = getUser(email);

        LongTermGoal goal = null;
        if (request.getLongTermGoalId() != null) {
            goal = goalRepository.findById(request.getLongTermGoalId())
                    .orElseThrow(() -> new CustomException(ErrorCode.GOAL_NOT_FOUND));
        }

        DailyPlan plan = planRepository.save(DailyPlan.builder()
                .user(user)
                .longTermGoal(goal)
                .date(request.getDate())
                .build());

        return new PlanResponse(plan, taskRepository.findByDailyPlan(plan));
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getByDate(String email, LocalDate date) {
        User user = getUser(email);
        return planRepository.findByUserAndDate(user, date).stream()
                .map(plan -> new PlanResponse(plan, taskRepository.findByDailyPlan(plan)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getById(String email, Long planId) {
        DailyPlan plan = getPlan(email, planId);
        return new PlanResponse(plan, taskRepository.findByDailyPlan(plan));
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAll(String email) {
        User user = getUser(email);
        return planRepository.findByUser(user).stream()
                .map(plan -> new PlanResponse(plan, taskRepository.findByDailyPlan(plan)))
                .toList();
    }

    @Transactional
    public void delete(String email, Long planId) {
        DailyPlan plan = getPlan(email, planId);
        planRepository.delete(plan);
    }

    @Transactional
    public TaskResponse addTask(String email, Long planId, TaskCreateRequest request) {
        DailyPlan plan = getPlan(email, planId);
        Task task = Task.builder()
                .dailyPlan(plan)
                .title(request.getTitle())
                .build();
        return new TaskResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(String email, Long planId) {
        DailyPlan plan = getPlan(email, planId);
        return taskRepository.findByDailyPlan(plan).stream()
                .map(TaskResponse::new)
                .toList();
    }

    private DailyPlan getPlan(String email, Long planId) {
        DailyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));
        if (!plan.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return plan;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}