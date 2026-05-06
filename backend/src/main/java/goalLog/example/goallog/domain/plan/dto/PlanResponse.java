package goalLog.example.goallog.domain.plan.dto;

import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PlanResponse {

    private Long id;
    private LocalDate date;
    private Long longTermGoalId; // 연결된 목표 id, 없으면 null
    private LocalDateTime createdAt;

    public PlanResponse(DailyPlan plan) {
        this.id = plan.getId();
        this.date = plan.getDate();
        this.longTermGoalId = plan.getLongTermGoal() != null ? plan.getLongTermGoal().getId() : null;
        this.createdAt = plan.getCreatedAt();
    }
}