# GoalLog — 목표 기록 시스템

사용자가 장기 목표를 설정하고, 일별 계획(DailyPlan)과 세부 Task로 세분화하여 체계적으로 관리할 수 있는 웹 기반 플래너 시스템입니다.

**팀원:** 이상윤 (20203115), 황연주 (20233123), 노민 에르덴 (20223579)

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 4.0.5, Spring Security, Spring Data JPA / Hibernate |
| Frontend | React 18, Vite 5, React Router DOM 6, Axios |
| Database | MySQL |
| 인증 | JWT (Access Token), BCrypt |
| 빌드 | Gradle (backend) / npm (frontend) |
| 테스트 | JUnit 5, MockMvc, Vitest, React Testing Library, JaCoCo |
| 배포 | Railway |

---

## 도메인 모델

```
User (1) ──< LongTermGoal (N)
User (1) ──< DailyPlan (N)
LongTermGoal (1) ──< DailyPlan (N)   [nullable — 목표 없이 플랜만 생성 가능]
DailyPlan (1) ──< Task (N)
```

### 테이블 목록 (MySQL)

| 테이블 | 주요 컬럼 |
|--------|-----------|
| `users` | id, email, password |
| `long_term_goals` | id, user_id, title, description, deadline, category, priority, status |
| `daily_plans` | id, user_id, long_term_goal_id (nullable), date |
| `tasks` | id, daily_plan_id, title, completed |

---

## 저장소 구조

```
goalLog/
├── AGENTS.md
├── README.md
├── backend/                        # Spring Boot 애플리케이션
│   └── src/main/java/goalLog/example/goallog/
│       ├── domain/
│       │   ├── user/               # 회원가입, 로그인
│       │   ├── goal/               # 장기 목표 CRUD + 진행률
│       │   ├── plan/               # 하루 계획 CRUD
│       │   └── task/               # Task CRUD + 완료 토글
│       └── global/
│           ├── common/             # ApiResponse (공통 응답 포맷)
│           ├── exception/          # CustomException, ErrorCode, GlobalExceptionHandler
│           └── security/           # JwtProvider, JwtFilter, SecurityConfig
├── frontend/                       # React 애플리케이션
│   └── src/
│       ├── pages/
│       │   ├── LoginPage.jsx
│       │   ├── SignupPage.jsx
│       │   ├── GoalListPage.jsx
│       │   ├── GoalDetailPage.jsx  # 진행률 시각화 포함
│       │   └── DailyPlanPage.jsx   # 날짜별 플랜 + Task 체크
│       ├── components/             # Navbar, ProgressBar, ProgressRing, TaskItem 등
│       ├── api/                    # Axios 인스턴스, 도메인별 API 함수
│       └── App.jsx
├── test/
│   ├── test-001/                   # Sprint 1 — 회원가입/로그인 테스트
│   ├── test-002/                   # Sprint 2 — 목표/플랜/Task CRUD 테스트
│   ├── test-003/                   # Sprint 3 — 진행률 계산 & E2E 테스트
│   ├── test-004/                   # Sprint 4 — UI 컴포넌트 & 에러 응답 테스트
│   ├── test-005/                   # Sprint 5 — 커버리지 측정 & 회귀 테스트
│   └── test-006/                   # Sprint 6 — 최종 E2E & 배포 검증
└── .github/workflows/              # CI/CD 파이프라인
```

---

## API 엔드포인트

### 인증 `/api/auth` — 인증 불필요

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → JWT 반환 |

### 장기 목표 `/api/goals` — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/goals` | 목표 생성 |
| GET | `/api/goals` | 내 목표 전체 조회 |
| GET | `/api/goals/{id}` | 목표 단건 조회 |
| PUT | `/api/goals/{id}` | 목표 수정 |
| DELETE | `/api/goals/{id}` | 목표 삭제 |
| GET | `/api/goals/{id}/progress` | 진행률 조회 (%) |
| GET | `/api/goals/{id}/plans` | 연결된 플랜 목록 |

### 하루 계획 `/api/plans` — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/plans` | 플랜 생성 |
| GET | `/api/plans?date=YYYY-MM-DD` | 날짜별 플랜 조회 |
| GET | `/api/plans/all` | 내 플랜 전체 조회 |
| DELETE | `/api/plans/{id}` | 플랜 삭제 |
| POST | `/api/plans/{planId}/tasks` | 플랜에 Task 추가 |
| GET | `/api/plans/{planId}/tasks` | 플랜의 Task 목록 |

### Task `/api/tasks` — JWT 필요

| Method | URL | 설명 |
|--------|-----|------|
| PATCH | `/api/tasks/{id}/toggle` | 완료 상태 토글 |
| PATCH | `/api/tasks/{id}` | 제목 수정 |
| DELETE | `/api/tasks/{id}` | Task 삭제 |

### 공통 응답 형식

```json
// 성공
{ "success": true, "data": { } }

// 실패
{ "success": false, "message": "에러 메시지" }
```

---

## 인증 흐름

1. 로그인 성공 시 JWT Access Token 발급
2. 이후 모든 요청 헤더에 `Authorization: Bearer <token>` 포함
3. JwtFilter가 토큰 검증 후 SecurityContext에 유저 정보 저장
4. Controller에서 `@AuthenticationPrincipal UserDetails`로 현재 유저 이메일 추출

---

## 실행 방법

### 사전 준비

- Java 17+, Node.js 18+, MySQL 8

### Backend

```bash
cd backend
./gradlew bootRun
# http://localhost:8080
```

`src/main/resources/application.properties` 설정:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/goallog
spring.datasource.username=<your-username>
spring.datasource.password=<your-password>
jwt.secret=<256bit-이상의-시크릿-키>
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# http://localhost:3000
```

### 테스트

```bash
# Backend (JaCoCo 커버리지 포함)
cd backend && ./gradlew test jacocoTestReport

# Frontend
cd frontend && npm run test
```

---

## 환경 변수 (Railway 배포)

| 변수명 | 설명 |
|--------|------|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | DB 유저명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (256bit 이상) |

---

## ErrorCode 목록

| 코드 | HTTP | 설명 |
|------|------|------|
| `USER_NOT_FOUND` | 404 | 유저 없음 |
| `EMAIL_DUPLICATE` | 409 | 이메일 중복 |
| `INVALID_PASSWORD` | 401 | 비밀번호 불일치 |
| `GOAL_NOT_FOUND` | 404 | 목표 없음 |
| `PLAN_NOT_FOUND` | 404 | 플랜 없음 |
| `TASK_NOT_FOUND` | 404 | Task 없음 |
| `UNAUTHORIZED` | 401 | 본인 리소스 아님 |

---

## 스프린트 계획

| Sprint | 기간 | 주요 개발 내용 |
|--------|------|----------------|
| Sprint 1 | ~5/11 | 초기 세팅, 회원가입/로그인(JWT), Spring Security 필터 |
| Sprint 2 | ~5/13 | 장기 목표 / 하루 계획 / Task CRUD API |
| Sprint 3 | ~5/18 | 진행률 계산 API, 프로그레스 바 UI, Railway 배포 |
| Sprint 4 | ~5/20 | React UI 통합 & UX 개선, 에러 핸들링 고도화 |
| Sprint 5 | ~5/25 | 코드 리팩터링, JaCoCo 커버리지 80%+ 달성 (현재 87%) |
| Sprint 6 | ~5/27 | 최종 E2E 테스트, Railway 최종 배포 검증, README 작성 |