package goalLog.example.goallog.domain.task.entity;

import goalLog.example.goallog.domain.plan.entity.DailyPlan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 태스크가 속한 하루 계획
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id", nullable = false)
    private DailyPlan dailyPlan;

    @Column(nullable = false)
    private String title;

    // 완료 여부 - 기본값 false
    @Column(nullable = false)
    private boolean completed = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }

    @Builder
    public Task(DailyPlan dailyPlan, String title) {
        this.dailyPlan = dailyPlan;
        this.title = title;
        this.completed = false;
    }

    // [GRASP: Information Expert] - Task 자신이 completed 상태를 알고 있으므로 직접 반전시키는 책임을 갖는다.
    public void toggleCompleted() {
        this.completed = !this.completed;
    }
}