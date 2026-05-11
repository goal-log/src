package goalLog.example.goallog.domain.task.dto;

import goalLog.example.goallog.domain.task.entity.Task;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskResponse {

    private Long id;
    private String title;
    private boolean completed;
    private LocalDateTime createdAt;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.completed = task.isCompleted();
        this.createdAt = task.getCreatedAt();
    }
}