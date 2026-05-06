package goalLog.example.goallog.domain.plan.entity;

import goalLog.example.goallog.domain.goal.entity.LongTermGoal;
import goalLog.example.goallog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이 플랜을 만든 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 연결된 장기 목표 - nullable, 목표 없이 하루 계획만 만들 수도 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "long_term_goal_id")
    private LongTermGoal longTermGoal;

    @Column(nullable = false)
    private LocalDate date; // 이 플랜이 해당하는 날짜

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public DailyPlan(User user, LongTermGoal longTermGoal, LocalDate date) {
        this.user = user;
        this.longTermGoal = longTermGoal;
        this.date = date;
    }
}