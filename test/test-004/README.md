# Sprint 4 — UI 컴포넌트 & 에러 응답 테스트

## 범위
- 백엔드: `GlobalExceptionHandler` 단위 + 모든 `ErrorCode`의 HTTP 응답 검증
- 프론트엔드: React 컴포넌트 단위 테스트 (Vitest + Testing Library)

## 백엔드 파일
| 파일 | 종류 | 검증 |
|------|------|------|
| [GlobalExceptionHandlerTest.java](GlobalExceptionHandlerTest.java) | 단위 | CustomException → 상태/메시지 매핑, 예상 못한 Exception → 500 |
| [ErrorResponseIT.java](ErrorResponseIT.java) | 통합 | 7개 ErrorCode 전체 응답 코드/본문 검증, 공통 응답 포맷 |

### 백엔드 실행
```bash
cd backend
./gradlew test --tests "goalLog.example.goallog.sprint4.*"
```

## 프론트엔드 (Vitest)

`frontend/` 에 Vitest + Testing Library 설정과 첫 컴포넌트 테스트가 포함되어 있다.

### 설정 파일
- `frontend/vitest.config.js`
- `frontend/src/setupTests.js`
- `frontend/src/components/__tests__/ProgressBar.test.jsx`

### 실행
```bash
cd frontend
npm install
npm test
```

### 검증
- `ProgressBar`: done/total 비율을 %로 정확히 렌더링, 100% 완료 시 `.complete` 클래스 부여, total=0 일 때 0% 처리

## ErrorCode 대응표
| 코드 | HTTP | 트리거 |
|------|------|--------|
| EMAIL_DUPLICATE | 409 | 동일 이메일 재가입 |
| USER_NOT_FOUND | 404 | 미가입 이메일 로그인 |
| INVALID_PASSWORD | 401 | 잘못된 비밀번호 로그인 |
| GOAL_NOT_FOUND | 404 | 존재하지 않는 goalId 조회 |
| PLAN_NOT_FOUND | 404 | 존재하지 않는 planId 삭제 |
| TASK_NOT_FOUND | 404 | 존재하지 않는 taskId 토글 |
| UNAUTHORIZED | 401 | 타인 리소스 접근 |
