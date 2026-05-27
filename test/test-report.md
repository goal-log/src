# GoalLog 스프린트 테스트 결과 보고서

작성일: 2026-05-27
대상 브랜치: `test/setup-sprint-tests` (커밋 `818a9a6`)

---

## 1. 요약 (Executive Summary)

| 항목 | 결과 |
|------|------|
| 백엔드 테스트 | **51개 모두 통과** (0 failed) |
| 프론트엔드 테스트 | **4개 모두 통과** (Vitest 스캐폴드) |
| JaCoCo 라인 커버리지 | **Instruction 87%, Branch 82%** |
| Sprint 5 목표 (80% 커버리지) | ✅ **달성** |
| 스프린트별 테스트 디렉토리 (1~6) | ✅ 모두 구현 |

**결론**: AGENTS.md 가 약속한 `test/test-001~006` 구조대로 스프린트 1~6 의 테스트 요구사항을 모두 충족하며, 백엔드 핵심 로직(인증, CRUD, 진행률, 에러 처리, 회귀, 다중 사용자 E2E)이 자동화된 안전망으로 보호된다.

---

## 2. 실행 방법

### 2.1 백엔드 (51개)
```bash
cd backend
./gradlew test
```
- H2 in-memory DB 사용 — MySQL 설치 불필요
- JWT 시크릿: 테스트 전용 키 (`test/resources/application-test.properties`)
- 실행 시간: 약 1분 20초 (Spring Context 부팅 포함)

### 2.2 스프린트 단위 실행
```bash
./gradlew test --tests "goalLog.example.goallog.sprint1.*"
./gradlew test --tests "goalLog.example.goallog.sprint2.*"
# ... sprint3, sprint4, sprint5, sprint6
```

### 2.3 JaCoCo 커버리지 리포트
```bash
cd backend
./gradlew test jacocoTestReport
```
리포트 위치: `backend/build/reports/jacoco/test/html/index.html` (브라우저로 열기)

### 2.4 80% 커버리지 게이트
```bash
./gradlew jacocoTestCoverageVerification
```
미달 시 빌드 실패.

### 2.5 프론트엔드 (4개)
```bash
cd frontend
npm install
npm test
```

---

## 3. 스프린트별 상세 결과

### Sprint 1 — 회원가입 / 로그인 (10개)

**검증 목표**: 이메일+비밀번호로 가입·로그인, BCrypt 암호화, JWT 발급, 이메일 중복 차단

#### Unit Test — [AuthServiceTest.java](test-001/AuthServiceTest.java) (5개)
| # | 케이스 | 검증 내용 |
|---|--------|----------|
| 1 | `signup_success` | 신규 이메일 → `passwordEncoder.encode()` 호출 + `userRepository.save()` 호출 |
| 2 | `signup_duplicate` | 기가입 이메일 → `CustomException(EMAIL_DUPLICATE)`, save 미호출 |
| 3 | `login_success` | 비밀번호 일치 → `JwtProvider.generateToken()` 반환값 그대로 응답 |
| 4 | `login_userNotFound` | 미가입 이메일 → `USER_NOT_FOUND` |
| 5 | `login_wrongPassword` | 비밀번호 불일치 → `INVALID_PASSWORD` |

#### Integration Test — [AuthControllerIT.java](test-001/AuthControllerIT.java) (5개)
HTTP 레벨 검증, MockMvc + 실 H2 DB 사용

| # | 케이스 | 검증 |
|---|--------|------|
| 1 | `POST /api/auth/signup` 신규 | 200 + `success:true` |
| 2 | `POST /api/auth/signup` 중복 | **409 Conflict** + `success:false` |
| 3 | `POST /api/auth/login` 성공 | 200 + `data.token` 비어있지 않음 |
| 4 | `POST /api/auth/login` 비번 불일치 | **401 Unauthorized** |
| 5 | `POST /api/auth/login` 미가입 | **404 Not Found** |

### Sprint 2 — Goal / Plan / Task CRUD (16개)

**검증 목표**: 도메인 3개의 CRUD 동작, 본인 소유 리소스만 접근, 도메인 간 관계 (Goal ↔ Plan ↔ Task)

#### [GoalServiceTest.java](test-002/GoalServiceTest.java) (5개)
- `create`: 저장 + 기본값 (`priority=MEDIUM`, `status=IN_PROGRESS`) 적용
- `getAll_onlyMine`: 본인 목표만 조회 (다른 유저 데이터 격리)
- `update`: 전체 필드 갱신 (title, description, deadline, category, priority, status)
- `delete`: 삭제 후 조회 시 `GOAL_NOT_FOUND`
- `ownership`: 다른 유저의 목표 조회/삭제 시 `UNAUTHORIZED`

#### [PlanServiceTest.java](test-002/PlanServiceTest.java) (7개)
- `create_withoutGoal`: `longTermGoalId=null` 도 OK (목표 없는 일일 플랜)
- `create_withGoal`: 장기 목표와 연결
- `create_goalNotFound`: 존재하지 않는 `longTermGoalId` → `GOAL_NOT_FOUND`
- `getByDate`: 본인 + 해당 날짜 플랜만 조회
- `addTask`: 새 Task 는 `completed=false` 로 저장
- `delete`: 삭제 후 `PLAN_NOT_FOUND`
- `ownership`: 다른 유저의 플랜 조회/Task 추가 시 `UNAUTHORIZED`

#### [TaskServiceTest.java](test-002/TaskServiceTest.java) (4개)
- `toggle`: `false → true → false` 반전 검증
- `delete`: 저장소에서 실제 제거
- `ownership`: 다른 유저의 Task 토글/삭제 시 `UNAUTHORIZED`
- `notFound`: 존재하지 않는 ID → `TASK_NOT_FOUND`

### Sprint 3 — 진행률 계산 & E2E (7개)

**검증 목표**: `getProgress()` 의 수식 정확성, 회원가입~진행률 조회 전체 HTTP 플로우

#### [GoalProgressTest.java](test-003/GoalProgressTest.java) (5개)
진행률 수식: `(completed * 100) / total` (정수 절삭)

| Total | Completed | 기대 % | 케이스 |
|-------|-----------|--------|--------|
| 0 | 0 | 0% | `noTasks` |
| 4 | 1 | 25% | `quarter` |
| 3 | 3 | 100% | `allDone` |
| 4 (2 plans) | 1 | 25% | `multiplePlans` — 여러 플랜의 Task 를 합산 |
| 3 | 1 | 33% | `truncation` — 33.3 → 33 절삭 확인 |

#### [EndToEndScenarioIT.java](test-003/EndToEndScenarioIT.java) (2개)
**시나리오 1: `fullFlow`**
```
POST /api/auth/signup
POST /api/auth/login           → JWT
POST /api/goals                → goalId
POST /api/plans                → planId
POST /api/plans/{planId}/tasks → t1, t2
PATCH /api/tasks/{t1}/toggle
GET  /api/goals/{goalId}/progress  → 50% ✓
PATCH /api/tasks/{t2}/toggle
GET  /api/goals/{goalId}/progress  → 100% ✓
```

**시나리오 2: `unauthorized_withoutToken`** — 토큰 없이 보호 API 호출 시 401/403 차단

### Sprint 4 — UI 컴포넌트 & 에러 응답 (16개)

**검증 목표**: 모든 ErrorCode 의 HTTP 상태/응답 본문 정확성, 공통 응답 포맷, React 컴포넌트 단위 테스트

#### 백엔드 — [GlobalExceptionHandlerTest.java](test-004/GlobalExceptionHandlerTest.java) (4개)
- `customException`: `GOAL_NOT_FOUND` → 404 + 메시지
- `emailDuplicate`: `EMAIL_DUPLICATE` → 409
- `unauthorizedFamily`: `INVALID_PASSWORD`, `UNAUTHORIZED` → 401
- `unexpectedException`: 예상 못한 `Exception` → 500 + "서버 오류가 발생했습니다."

#### 백엔드 — [ErrorResponseIT.java](test-004/ErrorResponseIT.java) (8개)
모든 `ErrorCode` 가 HTTP 통합 레벨에서 명세대로 반환되는지 검증

| 케이스 | 트리거 | 기대 응답 |
|--------|--------|----------|
| `emailDuplicate` | 동일 이메일 재가입 | 409 + 한국어 메시지 |
| `userNotFound` | 미가입 이메일 로그인 | 404 |
| `invalidPassword` | 잘못된 비밀번호 | 401 |
| `goalNotFound` | `GET /api/goals/99999` | 404 + "목표를 찾을 수 없습니다." |
| `planNotFound` | `DELETE /api/plans/99999` | 404 + "플랜을 찾을 수 없습니다." |
| `taskNotFound` | `PATCH /api/tasks/99999/toggle` | 404 + "태스크를 찾을 수 없습니다." |
| `otherUserResource` | 다른 유저 goal 접근 | 401 |
| `commonResponseShape` | 공통 포맷 검증 | 성공 `{success, data}` / 실패 `{success, message}` |

#### 프론트엔드 — [ProgressBar.test.jsx](../frontend/src/components/__tests__/ProgressBar.test.jsx) (4개)
- 25% 렌더링 정확성 (done=1, total=4)
- 100% 시 `.complete` 클래스 부여
- `total=0` 안전 처리 → 0%
- 33.3% 반올림 → 33% (백엔드와 동일 동작)

### Sprint 5 — 회귀 테스트 & 커버리지 (4개)

**검증 목표**: GRASP 패턴 리팩터링(주석 추가, commit `65ea5e6`) 이후에도 핵심 흐름이 깨지지 않았는지 빠르게 검증 + JaCoCo 80% 게이트

#### [RegressionIT.java](test-005/RegressionIT.java) — 순차 4단계
| Order | 단계 | 검증 |
|-------|------|------|
| 1 | `step01_auth` | 가입 + 로그인 + JWT 발급 |
| 2 | `step02_crud` | Goal → Plan → Task 연쇄 생성 |
| 3 | `step03_progress` | Task 토글 → 진행률 100% |
| 4 | `step04_cleanup` | Task → Plan 순 삭제, Goal 유지 |

`@TestInstance(PER_CLASS)` + `@Order(N)` 로 한 인스턴스 안에서 상태 누적, `@AfterAll` 에서 정리.

#### JaCoCo 커버리지 결과

전체: **Instruction 87% / Branch 82%**

| 패키지 | Instruction Cov |
|--------|----|
| `domain.user.service` (AuthService) | **100%** |
| `domain.task.service` | **100%** |
| `domain.user.controller` | **100%** |
| `domain.task.controller` | **100%** |
| `domain.user.entity` | **100%** |
| `domain.task.entity` | **100%** |
| `domain.plan.entity` | **100%** |
| `domain.goal.dto` | **100%** |
| `domain.task.dto` | **100%** |
| `global.common` (ApiResponse) | **100%** |
| `global.exception` | **100%** |
| `global.security` (Jwt 필터/프로바이더) | 97% |
| `domain.goal.entity` | 87% |
| `domain.plan.dto` | 86% |
| `domain.goal.service` | 90% |
| `domain.plan.service` | 70% |
| `domain.goal.controller` | 54% |
| `domain.plan.controller` | 45% |

**미커버 영역** (참고): 일부 Controller 메서드 (예: `GET /api/goals/{id}/plans`, `GET /api/plans/all`) 는 직접 HTTP 호출하는 테스트가 없음 — Service 레벨로 90%+ 커버. Sprint 6 또는 후속 작업에서 보강 가능.

### Sprint 6 — 최종 E2E & 배포 검증 (2개)

**검증 목표**: 다중 사용자 격리, 진행률 다단계 변화, 잘못된 JWT 차단, Railway 배포 후 수동 스모크 테스트 절차

#### [FinalE2EIT.java](test-006/FinalE2EIT.java)
**시나리오 1: `multiUserScenario`** — 사용자 A/B 동시 운영
- A: 목표 + 플랜 + Task 3개 / B: 목표 + 플랜 + Task 2개
- A 가 B 의 목표/Task 접근 시 401 차단 검증
- A 의 진행률 전이: `0 → 33% → 66% → 100% → 66%(해제) → 100%(Task 삭제 시 재계산)`
- A 의 모든 활동 동안 B 의 진행률은 0% 로 유지 (격리)
- B 가 본인 Task 토글 시 50% 만 영향

**시나리오 2: `invalidToken`** — 위조된 JWT 헤더로 보호 API 호출 시 401/403 차단

#### Railway 배포 검증 절차 — [test-006/README.md](test-006/README.md)
배포 URL 을 `$BASE` 로 두고 `curl + jq` 로 다음을 단계별 확인:
1. `POST /api/auth/signup`, `POST /api/auth/login` → token
2. Goal → Plan → Task 생성
3. PATCH toggle → GET progress (100% 확인)
4. 리소스 정리 (DELETE)
5. 체크리스트: HTTPS, CORS, 환경변수 분리, 프론트엔드 baseURL

---

## 4. 인프라

### 4.1 디렉토리 구조
```
test/
├── SPRINT_TEST_REPORT.md          # 이 문서
├── resources/
│   └── application-test.properties # H2 + 테스트용 JWT
├── test-001/  Sprint 1
│   ├── AuthServiceTest.java
│   ├── AuthControllerIT.java
│   ├── TestDbCleaner.java         # FK 순서 정리 헬퍼 (공용)
│   └── README.md
├── test-002/  Sprint 2
├── test-003/  Sprint 3
├── test-004/  Sprint 4
├── test-005/  Sprint 5
└── test-006/  Sprint 6
```

### 4.2 Gradle 설정 ([backend/build.gradle](../backend/build.gradle))
```gradle
sourceSets {
    test {
        java { srcDirs += ['../test/test-001', ..., '../test/test-006'] }
        resources { srcDirs += ['../test/resources'] }
    }
}

jacoco { toolVersion = '0.8.12' }
tasks.named('test') { useJUnitPlatform(); finalizedBy 'jacocoTestReport' }
tasks.named('jacocoTestCoverageVerification') {
    violationRules {
        rule { limit { counter = 'INSTRUCTION'; minimum = 0.80 } }
    }
}
```

### 4.3 환경 결정 사항
- **DB**: H2 in-memory (MySQL 호환 모드), `create-drop` — 클래스간 데이터 누수 차단을 위해 `TestDbCleaner` 가 FK 순서(`tasks → plans → goals → users`)로 비움
- **Jackson**: Spring Boot 4 의 Jackson 3.x 사용 — `tools.jackson.databind.*` 임포트
- **MockMvc**: Spring Boot 4 에서 `@AutoConfigureMockMvc` 가 변경되어 `MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build()` 로 수동 빌드
- **Jackson 가시성**: `application-test.properties` 에서 `spring.jackson.visibility.field=any` — DTO 가 `@Getter` 만 가지고 setter 가 없어도 JSON 역직렬화 가능

---

## 5. 검증된 비즈니스 규칙

### 5.1 인증
- 이메일 중복 가입 불가 → 409
- BCrypt 암호화 저장
- 잘못된 비밀번호 → 401 (USER_NOT_FOUND 와 구분)
- JWT 발급, 무인증 요청 차단, 변조 토큰 차단

### 5.2 소유권 (Ownership)
모든 도메인(Goal/Plan/Task) 에서 본인이 아닌 리소스 접근 시 **UNAUTHORIZED** 일관성 검증:
- 다른 유저의 목표 조회/수정/삭제
- 다른 유저의 플랜 조회/Task 추가
- 다른 유저의 Task 토글/삭제

### 5.3 진행률
- `total=0` → 0% (0 나눗셈 방지)
- 정수 절삭 (`33.3 → 33`) — 백엔드/프론트엔드 동일
- 여러 플랜의 Task 합산
- Task 삭제 시 즉시 재계산

### 5.4 응답 포맷
- 성공: `{"success": true, "data": ...}`
- 실패: `{"success": false, "message": "..."}`
- HTTP 상태와 본문이 모두 ErrorCode 명세와 일치

---

## 6. 테스트 안 한 것 / 한계

- **CORS 동작**: `SecurityConfig` 의 CORS 설정은 코드만 있고 실제 cross-origin 요청 테스트 없음
- **JWT 만료**: `validateToken` 의 만료 분기는 미커버 (현재 expiration 24시간)
- **DB 동시성**: 두 클라이언트가 동일 Task 동시 토글 시 락 동작 — 미검증
- **페이지네이션**: 현재 API 는 페이지 없는 전체 조회. 대량 데이터 케이스 미검증
- **Swagger**: Sprint 4 계획상 포함이었으나 미구현
- **Railway 실제 배포**: 절차 문서화 완료, 실제 배포 URL 에 대한 자동화 스모크 테스트는 미실행

---

## 7. 알려진 운영 코드 이슈 (테스트 작성 중 발견)

테스트가 통과한다고 운영 코드에 이슈가 없는 건 아닙니다. 다음 5개는 다음 리팩터링 후보:

1. **DTO 검증 누락** — `SignupRequest` 등에 `@NotBlank` 없음 → 빈 문자열 가입 가능. AGENTS.md 규약 위반
2. **DailyPlan ↔ Task cascade 없음** — Task 가 있는 Plan 삭제 시 FK 위반 (회귀 테스트 `step04` 에서 발견, Task 먼저 삭제로 우회)
3. **`GoalService.getProgress()` N+1** — 플랜 1개당 쿼리 2개 발생
4. **`PlanService` stream N+1** — `getByDate`, `getAll` 등에서 plan 마다 `findByDailyPlan` 호출
5. **컨트롤러 인증 추출 중복** — `@AuthenticationPrincipal UserDetails userDetails; userDetails.getUsername()` 17 회 반복

---

## 8. 결론

| 평가 항목 | 결과 |
|-----------|------|
| Sprint 1~6 테스트 요구사항 충족 | ✅ 100% |
| AGENTS.md `test/test-001~006` 구조 준수 | ✅ |
| Sprint 5 커버리지 80% 목표 | ✅ 87% (Instruction) 달성 |
| 핵심 비즈니스 규칙(인증, 소유권, 진행률) 자동 검증 | ✅ |
| 회귀 안전망 (리팩터링 후 깨짐 감지) | ✅ |
| 다중 사용자 격리 검증 | ✅ |
| 실 배포 검증 절차 문서화 | ✅ (실행은 미진행) |

테스트 인프라가 마련되어, 이후 모든 변경은 `./gradlew test` 한 줄로 51개의 안전망을 거치게 된다. 다음 단계 권장 작업은 (1) Railway 배포 + 스모크 실행, (2) 위 7장의 운영 코드 이슈 리팩터링이다.

---

**참고 파일**
- 빌드 설정: [backend/build.gradle](../backend/build.gradle)
- 테스트 환경: [test/resources/application-test.properties](resources/application-test.properties)
- 헬퍼: [TestDbCleaner.java](test-001/TestDbCleaner.java)
- 스프린트별 상세 README: [test-001](test-001/README.md) · [test-002](test-002/README.md) · [test-003](test-003/README.md) · [test-004](test-004/README.md) · [test-005](test-005/README.md) · [test-006](test-006/README.md)
