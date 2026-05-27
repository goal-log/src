# Sprint 3 — 진행률 계산 & E2E 시나리오 테스트

## 범위
- 진행률 계산 로직 단위 검증 (0%, 25%, 33%, 50%, 100%, multi-plan 합산)
- 회원가입부터 진행률 조회까지 전체 API 시나리오 검증

## 파일
| 파일 | 종류 | 검증 |
|------|------|------|
| [GoalProgressTest.java](GoalProgressTest.java) | 단위 | `getProgress()` — Task 0/1/3/4 케이스, 다중 플랜 합산, 33% 절삭 |
| [EndToEndScenarioIT.java](EndToEndScenarioIT.java) | E2E | signup → login → goal → plan → task → toggle → progress 흐름, 무인증 차단 |

## 시나리오 흐름
```
POST /api/auth/signup
POST /api/auth/login           → JWT
POST /api/goals                → goalId
POST /api/plans                → planId (longTermGoalId=goalId)
POST /api/plans/{planId}/tasks → t1, t2
PATCH /api/tasks/{t1}/toggle
GET  /api/goals/{goalId}/progress  → 50%
PATCH /api/tasks/{t2}/toggle
GET  /api/goals/{goalId}/progress  → 100%
```

## 실행
```bash
cd backend
./gradlew test --tests "goalLog.example.goallog.sprint3.*"
```
