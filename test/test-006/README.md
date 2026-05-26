# Sprint 6 — 최종 E2E & 배포 검증

## 범위
- 다중 사용자 시나리오 종합 검증
- Railway 배포 URL 스모크 테스트 절차

## 파일
| 파일 | 종류 | 검증 |
|------|------|------|
| [FinalE2EIT.java](FinalE2EIT.java) | 최종 E2E | 사용자 A/B 격리, 진행률 0→33→66→100→66→100% 전이, 다른 유저 리소스 차단, 잘못된 JWT 차단 |

## 로컬 최종 E2E 실행
```bash
cd backend
./gradlew test --tests "goalLog.example.goallog.sprint6.*"
```

## Railway 배포 후 수동 스모크 테스트

배포 URL을 `$BASE` 로 가정 (예: `https://goallog.up.railway.app`).

### 1) 회원가입 + 로그인
```bash
curl -X POST "$BASE/api/auth/signup" \
  -H 'Content-Type: application/json' \
  -d '{"email":"smoke@example.com","password":"smoke1234!"}'

TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"smoke@example.com","password":"smoke1234!"}' \
  | jq -r '.data.token')

echo "$TOKEN"   # 비어있지 않아야 함
```

### 2) 목표 → 플랜 → Task
```bash
GOAL=$(curl -s -X POST "$BASE/api/goals" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Smoke"}' | jq -r '.data.id')

PLAN=$(curl -s -X POST "$BASE/api/plans" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"date\":\"2026-05-27\",\"longTermGoalId\":$GOAL}" | jq -r '.data.id')

TASK=$(curl -s -X POST "$BASE/api/plans/$PLAN/tasks" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Verify deploy"}' | jq -r '.data.id')
```

### 3) 토글 + 진행률 확인
```bash
curl -s -X PATCH "$BASE/api/tasks/$TASK/toggle" \
  -H "Authorization: Bearer $TOKEN" | jq

curl -s "$BASE/api/goals/$GOAL/progress" \
  -H "Authorization: Bearer $TOKEN" | jq
# .data.progressPercent == 100 이면 ✅
```

### 4) 정리
```bash
curl -s -X DELETE "$BASE/api/plans/$PLAN" -H "Authorization: Bearer $TOKEN"
curl -s -X DELETE "$BASE/api/goals/$GOAL" -H "Authorization: Bearer $TOKEN"
```

## 배포 체크리스트
- [ ] `$BASE/api/auth/signup` 200 응답
- [ ] `$BASE/api/auth/login` 200 + token 발급
- [ ] JWT 헤더로 보호 API 접근 가능
- [ ] CORS: 프론트엔드 도메인에서 호출 가능
- [ ] 환경변수 `DB_URL`, `JWT_SECRET` 등 분리 설정됨
- [ ] HTTPS 적용 (Railway 기본 제공)
- [ ] 프론트엔드가 `$BASE` 를 baseURL로 사용
