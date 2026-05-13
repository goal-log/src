package goalLog.example.goallog.domain.goal.dto;

import goalLog.example.goallog.domain.goal.entity.LongTermGoal;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class GoalResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate deadline;
    private String category;
    private String priority;
    private String status;
    private LocalDateTime createdAt;

    public GoalResponse(LongTermGoal goal) {
        this.id = goal.getId();
        this.title = goal.getTitle();
        this.description = goal.getDescription();
        this.deadline = goal.getDeadline();
        this.category = goal.getCategory();
        this.priority = goal.getPriority();
        this.status = goal.getStatus();
        this.createdAt = goal.getCreatedAt();
    }
}