package goalLog.example.goallog.domain.goal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalUpdateRequest {

    private String title;
    private String description;
    private LocalDate deadline;
}