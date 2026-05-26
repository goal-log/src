# Sprint 5 — 커버리지 측정 & 회귀 테스트

## 범위
- JaCoCo 커버리지 측정 (Instruction 기준 80% 목표)
- 리팩터링(GRASP 주석 추가 등) 이후에도 핵심 흐름이 깨지지 않았는지 회귀 확인

## 파일
| 파일 | 종류 | 검증 |
|------|------|------|
| [RegressionIT.java](RegressionIT.java) | 회귀 | signup→login→CRUD→toggle→progress→cleanup 흐름이 한 번에 통과 |

## JaCoCo 커버리지 측정

### 측정 + HTML 리포트
```bash
cd backend
./gradlew clean test jacocoTestReport
```

리포트 위치: `backend/build/reports/jacoco/test/html/index.html`

브라우저로 열어서 클래스별 instruction/branch 커버리지를 확인한다.

### 80% 게이트 검증
`build.gradle` 의 `jacocoTestCoverageVerification` 태스크가 Instruction 80% 이상을 강제한다.
```bash
cd backend
./gradlew jacocoTestCoverageVerification
```
미달 시 빌드 실패.

### CI 연동 (예시)
```yaml
# .github/workflows/test.yml
- run: cd backend && ./gradlew test jacocoTestCoverageVerification
- uses: actions/upload-artifact@v4
  with:
    name: jacoco-report
    path: backend/build/reports/jacoco/test/html
```

## 회귀 테스트 실행
```bash
cd backend
./gradlew test --tests "goalLog.example.goallog.sprint5.*"
```

## 커버리지 향상 가이드
현재 미커버 영역이 식별되면:
1. 도메인별 (user, goal, plan, task) Service 분기 확인 — 본인 소유 검증 분기는 sprint2에서 커버 중
2. 보안 필터 (JwtFilter): 유효/무효 토큰, 헤더 없음 케이스 — sprint3 E2E에서 부분 커버
3. JwtProvider.validateToken(): 만료/변조 토큰 분기 → 추가 단위 테스트 권장
