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
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환
    public GoalResponse(LongTermGoal goal) {
        this.id = goal.getId();
        this.title = goal.getTitle();
        this.description = goal.getDescription();
        this.deadline = goal.getDeadline();
        this.createdAt = goal.getCreatedAt();
    }
}