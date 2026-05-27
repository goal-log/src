# Sprint 2 — 장기 목표 / 하루 계획 / Task CRUD 테스트

## 범위
- `GoalService`, `PlanService`, `TaskService` 비즈니스 로직 검증
- 본인 소유 리소스만 접근 가능한지 (UNAUTHORIZED 처리) 확인
- 도메인 간 연결: Goal ↔ Plan ↔ Task

## 파일
| 파일 | 검증 |
|------|------|
| [GoalServiceTest.java](GoalServiceTest.java) | 목표 생성/조회/수정/삭제, 본인 목표만 조회, 다른 유저 접근 차단 |
| [PlanServiceTest.java](PlanServiceTest.java) | 플랜 생성 (목표 연결 / 비연결), 날짜별 조회, Task 추가, 삭제, ownership |
| [TaskServiceTest.java](TaskServiceTest.java) | 완료 토글 (false ↔ true), 삭제, ownership, TASK_NOT_FOUND |

## 구현 방식
- `@SpringBootTest` + `@Transactional` — 실제 H2 DB와 JPA 영속성 컨텍스트를 그대로 사용
- 각 테스트 메서드 종료 시 트랜잭션 롤백 → 테스트 간 독립성 보장

## 실행
```bash
cd backend
./gradlew test --tests "goalLog.example.goallog.sprint2.*"
```
