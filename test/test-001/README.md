# Sprint 1 — 회원가입 / 로그인 테스트

## 범위
- `AuthService` 단위 테스트 (Mockito)
- `/api/auth/signup`, `/api/auth/login` 통합 테스트 (MockMvc + H2)

## 파일
| 파일 | 종류 | 검증 |
|------|------|------|
| [AuthServiceTest.java](AuthServiceTest.java) | 단위 | signup 중복 체크 / BCrypt 호출, login 성공·USER_NOT_FOUND·INVALID_PASSWORD |
| [AuthControllerIT.java](AuthControllerIT.java) | 통합 | 200/401/404/409 응답, JWT 토큰 발급 형식 |

## 실행
```bash
cd backend
./gradlew test --tests "goalLog.example.goallog.sprint1.*"
```
