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


    // 1. title 필드
    @Column(nullable = false)
    private String title;
    // 2. description 필드
    @Column(columnDefinition = "TEXT")
    private String description;
    // 3. deadline 필드
    private LocalDate deadline;
    // 4. createdAt 필드
    @Column(updatable = false)
    private LocalDateTime createdAt;
    // 5. @PrePersist 메서드
    @PrePersist // DB에 처음 저장될 때 자동으로 현재시간 세팅
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    // 6. @Builder 생성자
    @Builder
    public LongTermGoal(User user, String title, String description, LocalDate deadline) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }
    // 7. update() 메서드 , setter 대신 명시적인 수정 메서드 -> 수정 API에서 호출
    public void update(String title, String description, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }
}