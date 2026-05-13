package goalLog.example.goallog.domain.plan.dto;

import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import goalLog.example.goallog.domain.task.dto.TaskResponse;
import goalLog.example.goallog.domain.task.entity.Task;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PlanResponse {

    private Long id;
    private LocalDate date;
    private Long longTermGoalId;
    private LocalDateTime createdAt;
    private List<TaskResponse> tasks;

    public PlanResponse(DailyPlan plan, List<Task> tasks) {
        this.id = plan.getId();
        this.date = plan.getDate();
        this.longTermGoalId = plan.getLongTermGoal() != null ? plan.getLongTermGoal().getId() : null;
        this.createdAt = plan.getCreatedAt();
        this.tasks = tasks.stream().map(TaskResponse::new).toList();
    }

    public PlanResponse(DailyPlan plan) {
        this(plan, List.of());
    }
}