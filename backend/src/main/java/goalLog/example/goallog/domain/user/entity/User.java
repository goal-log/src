package goalLog.example.goallog.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // MySQL에 "user"는 예약어라 복수형으로 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요한데, 외부에서 직접 new User() 못 하게 막음
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB auto_increment
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt로 암호화된 값이 저장됨

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist // DB에 처음 저장될 때 자동으로 현재 시간 넣기
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}