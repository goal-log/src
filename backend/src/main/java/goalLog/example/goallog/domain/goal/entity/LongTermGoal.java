package goalLog.example.goallog.domain.goal.entity;

import goalLog.example.goallog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "long_term_goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LongTermGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate deadline;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String priority = "MEDIUM";

    @Column(length = 20)
    private String status = "IN_PROGRESS";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.priority == null) this.priority = "MEDIUM";
        if (this.status == null) this.status = "IN_PROGRESS";
    }

    @Builder
    public LongTermGoal(User user, String title, String description, LocalDate deadline,
                        String category, String priority, String status) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.category = category;
        this.priority = priority != null ? priority : "MEDIUM";
        this.status = status != null ? status : "IN_PROGRESS";
    }

    public void update(String title, String description, LocalDate deadline,
                       String category, String priority, String status) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.category = category;
        if (priority != null) this.priority = priority;
        if (status != null) this.status = status;
    }
}