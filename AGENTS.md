# GoalLog AI 에이전트 가이드

## 프로젝트 개요
GoalLog는 사용자가 장기 목표를 설정하고, 이를 일별 계획(DailyPlan)과 세부 Task로 세분화하여 체계적으로 관리할 수 있는 웹 기반 플래너 시스템입니다.

팀원: 이상윤 (20203115), 황연주 (20233123), 노민 에르덴 (20223579)  
개발 범위: 전체 시스템 설계, 백엔드, 프론트엔드, DB 설계

## 기술 스택
| 영역 | 기술 |
|------|------|
| 백엔드 | Spring Boot, Spring Security, JPA/Hibernate |
| 프론트엔드 | React, Axios |
| 데이터베이스 | MySQL |
| 인증 | JWT (Access Token), BCrypt |
| 배포 | Railway |
| 빌드 | Gradle |

## 저장소 구조
```
goalLog/
├── AGENTS.md
├── README.md
├── src/
│   ├── backend/               # Spring Boot 애플리케이션
│   └── frontend/              # React 애플리케이션
├── docs/
│   ├── user-story/            # 유저 스토리 문서
│   ├── scenario/              # 시나리오 문서
│   ├── uml/                   # 클래스 다이어그램, 시퀀스 다이어그램
│   └── api-spec/              # API 명세 문서
├── test/
│   ├── test-001/              # Sprint 1 - 회원가입/로그인 테스트
│   ├── test-002/              # Sprint 2 - 목표/플랜/Task CRUD 테스트
│   ├── test-003/              # Sprint 3 - 진행률 계산 & E2E 테스트
│   ├── test-004/              # Sprint 4 - UI 컴포넌트 & 에러 응답 테스트
│   ├── test-005/              # Sprint 5 - 커버리지 측정 & 회귀 테스트
│   └── test-006/              # Sprint 6 - 최종 E2E & 배포 검증
└── .github/workflows/         # CI/CD 파이프라인
```

## 도메인 모델
```
User (1) ──< LongTermGoal (N)
User (1) ──< DailyPlan (N)
LongTermGoal (1) ──< DailyPlan (N)   [nullable — 목표 없이 플랜만 생성 가능]
DailyPlan (1) ──< Task (N)
```

## 테이블 목록 (MySQL)
- `users` — 회원 정보 (id, email, password)
- `long_term_goals` — 장기 목표 (id, user_id, title, description, deadline)
- `daily_plans` — 하루 계획 (id, user_id, long_term_goal_id, date)
- `tasks` — 세부 할 일 (id, daily_plan_id, title, completed)

## 백엔드 패키지 구조
```
backend/src/main/java/goalLog/example/goallog/
├── domain/
│   ├── user/           # 회원가입, 로그인
│   ├── goal/           # 장기 목표 CRUD + 진행률
│   ├── plan/           # 하루 계획 CRUD
│   └── task/           # Task CRUD + 완료 토글
└── global/
    ├── common/         # ApiResponse (공통 응답 포맷)
    ├── exception/      # CustomException, ErrorCode, GlobalExceptionHandler
    └── security/       # JwtProvider, JwtFilter, SecurityConfig
```

## API 엔드포인트

### 인증 (/api/auth) — 인증 불필요
| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/auth/signup | 회원가입 |
| POST | /api/auth/login | 로그인 → JWT 반환 |

### 장기 목표 (/api/goals) — JWT 필요
| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/goals | 목표 생성 |
| GET | /api/goals | 내 목표 전체 조회 |
| GET | /api/goals/{id} | 목표 단건 조회 |
| PUT | /api/goals/{id} | 목표 수정 |
| DELETE | /api/goals/{id} | 목표 삭제 |
| GET | /api/goals/{id}/progress | 진행률 조회 (%) |

### 하루 계획 (/api/plans) — JWT 필요
| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/plans | 플랜 생성 |
| GET | /api/plans?date=YYYY-MM-DD | 날짜별 플랜 조회 |
| GET | /api/plans/all | 내 플랜 전체 조회 |
| DELETE | /api/plans/{id} | 플랜 삭제 |
| POST | /api/plans/{planId}/tasks | 플랜에 Task 추가 |

### Task (/api/tasks) — JWT 필요
| Method | URL | 설명 |
|--------|-----|------|
| PATCH | /api/tasks/{id}/toggle | 완료 상태 토글 |
| PATCH | /api/tasks/{id} | 제목 수정 |
| DELETE | /api/tasks/{id} | Task 삭제 |

## 공통 응답 형식
```json
// 성공
{ "success": true, "data": { } }

// 실패
{ "success": false, "message": "에러 메시지" }
```

## 인증 흐름
1. 로그인 성공 시 JWT Access Token 발급
2. 이후 모든 요청 헤더에 `Authorization: Bearer <token>` 포함
3. JwtFilter가 토큰 검증 후 SecurityContext에 유저 정보 저장
4. Controller에서 `@AuthenticationPrincipal UserDetails`로 현재 유저 이메일 추출

## 프론트엔드 구조 (React)
```
frontend/src/
├── pages/
│   ├── LoginPage.jsx
│   ├── SignupPage.jsx
│   ├── GoalListPage.jsx
│   ├── GoalDetailPage.jsx      # 진행률 시각화 포함
│   └── DailyPlanPage.jsx       # 날짜별 플랜 + Task 체크
├── components/
├── api/                        # Axios 인스턴스, API 함수
└── App.jsx
```
- JWT 토큰은 localStorage에 저장
- Axios 인터셉터로 모든 요청에 토큰 자동 첨부
- 진행률은 progress bar로 시각화

## 코드 컨벤션

### 백엔드
- 비즈니스 로직은 Service 계층에만 위치
- Controller는 요청/응답 처리만 담당
- 전역 예외는 GlobalExceptionHandler에서 처리
- Entity 수정은 setter 대신 명시적 메서드 (`update()`, `toggleCompleted()`)
- 조회: `@Transactional(readOnly = true)` / 변경: `@Transactional`
- 소유권 검증 필수 — 본인 리소스 아니면 `ErrorCode.UNAUTHORIZED` throw
- Request DTO에 `@NotBlank` / `@NotNull` 선언, Controller에 `@Valid` 적용

### 프론트엔드
- API 호출은 `api/` 폴더에서 관리
- 페이지 컴포넌트와 UI 컴포넌트 분리

## ErrorCode 목록
| 코드 | HTTP | 설명 |
|------|------|------|
| USER_NOT_FOUND | 404 | 유저 없음 |
| EMAIL_DUPLICATE | 409 | 이메일 중복 |
| INVALID_PASSWORD | 401 | 비밀번호 불일치 |
| GOAL_NOT_FOUND | 404 | 목표 없음 |
| PLAN_NOT_FOUND | 404 | 플랜 없음 |
| TASK_NOT_FOUND | 404 | Task 없음 |
| UNAUTHORIZED | 401 | 본인 리소스 아님 |

## 환경 변수 (Railway 배포)
| 변수명 | 설명 |
|--------|------|
| DB_URL | MySQL JDBC URL |
| DB_USERNAME | DB 유저명 |
| DB_PASSWORD | DB 비밀번호 |
| JWT_SECRET | JWT 서명 키 (256bit 이상) |

## Use Case 요약
- **회원가입 / 로그인** — 이메일 + 비밀번호, 이메일 중복 불가
- **장기 목표 관리** — 제목, 설명, 마감일 설정 / 수정 / 삭제
- **하루 계획 관리** — 날짜별 생성, 장기 목표와 연결 가능
- **Task 관리** — 세부 할 일 등록 / 완료 체크 / 수정 / 삭제
- **진행률 조회** — 장기 목표별 Task 달성률을 퍼센트로 시각화

## 스프린트 계획
| Sprint | 기간 | 개발 | 테스트 |
|--------|------|------|--------|
| Sprint 1 | ~5/11 | 초기 세팅, 회원가입/로그인(JWT), User 엔티티, Spring Security 필터 | AuthService 단위 테스트, POST /auth/signup 통합 테스트, POST /auth/login + JWT 검증 |
| Sprint 2 | ~5/13 | 장기 목표 CRUD API, 하루 계획 CRUD API, Task 등록·체크·삭제 API | GoalService 단위 테스트, CRUD API MockMvc 통합 테스트, Task 완료 체크 동작 검증 |
| Sprint 3 | ~5/18 | 진행률 계산 API (달성률 %), 프로그레스 바 시각화 UI, Railway 배포 & 환경 설정 | 진행률 계산 로직 단위 테스트, 전체 플로우 E2E 시나리오 테스트, 배포 후 API 연동 검증 |
| Sprint 4 | ~5/20 | React UI 통합 & UX 개선, API 에러 핸들링 고도화, Swagger API 문서 자동화 | UI 컴포넌트 단위 테스트, 에러 응답 코드 검증 |
| Sprint 5 | ~5/25 | AI-assisted 코드 리팩터링, JaCoCo 커버리지 측정, PR 기반 코드 리뷰 | 커버리지 80% 이상 달성, 리팩터링 후 회귀 테스트 |
| Sprint 6 | ~5/27 | 최종 통합 테스트 & 버그 수정, Railway 최종 배포 검증, README 작성 | 전체 E2E 최종 시나리오 검증, 배포 URL 접속 최종 확인 |