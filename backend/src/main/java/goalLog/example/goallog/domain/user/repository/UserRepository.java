package goalLog.example.goallog.domain.user.repository;

import goalLog.example.goallog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<엔티티 타입, PK 타입> 을 상속하면
// save, findById, findAll, delete 같은 기본 CRUD가 공짜로 생김
public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인할 때 이메일로 유저 찾기
    // "findBy + 필드명" 규칙만 지키면 JPA가 쿼리 자동 생성
    // → SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // 회원가입 때 이메일 중복 체크용
    // → SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}