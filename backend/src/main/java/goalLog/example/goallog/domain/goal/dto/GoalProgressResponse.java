package goalLog.example.goallog.domain.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalProgressResponse {

    private long totalTasks;    // 이 목표에 연결된 전체 태스크 수
    private long completedTasks; // 완료된 태스크 수
    private int progressPercent; // 진행률 (0~100)
}