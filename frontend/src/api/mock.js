// 백엔드 없이 디자인 확인용 mock 데이터

export const CATEGORIES = ['전체', '학습', '건강', '커리어', '재정', '취미', '기타'];
export const PRIORITIES = ['HIGH', 'MEDIUM', 'LOW'];
export const STATUSES = ['IN_PROGRESS', 'COMPLETED', 'PAUSED'];

export const MOCK_GOALS = [
  {
    id: 1,
    title: '토익 900점 달성',
    description: '매일 단어 50개, 리스닝 1시간 학습으로 6개월 안에 목표 달성',
    deadline: '2026-11-30',
    createdAt: '2026-05-01T09:00:00',
    category: '학습',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
  },
  {
    id: 2,
    title: '운동 습관 만들기',
    description: '주 5회 헬스장 방문, 유산소 30분 + 근력 운동 1시간',
    deadline: '2026-08-31',
    createdAt: '2026-05-03T10:00:00',
    category: '건강',
    priority: 'HIGH',
    status: 'IN_PROGRESS',
  },
  {
    id: 3,
    title: '사이드 프로젝트 출시',
    description: 'GoalLog MVP 완성 후 Product Hunt 런칭',
    deadline: '2026-07-15',
    createdAt: '2026-05-10T14:00:00',
    category: '커리어',
    priority: 'MEDIUM',
    status: 'IN_PROGRESS',
  },
  {
    id: 4,
    title: '비상금 500만원 모으기',
    description: '매달 50만원씩 저축, 불필요한 지출 줄이기',
    deadline: '2026-12-31',
    createdAt: '2026-04-01T08:00:00',
    category: '재정',
    priority: 'MEDIUM',
    status: 'PAUSED',
  },
  {
    id: 5,
    title: '기타 독학 완성',
    description: '코드 3개 마스터 후 좋아하는 곡 연주',
    deadline: '2026-06-30',
    createdAt: '2026-03-15T11:00:00',
    category: '취미',
    priority: 'LOW',
    status: 'COMPLETED',
  },
];

export const MOCK_PROGRESS = {
  1: { totalTasks: 20, completedTasks: 13, progressPercent: 65 },
  2: { totalTasks: 10, completedTasks: 4, progressPercent: 40 },
  3: { totalTasks: 8, completedTasks: 2, progressPercent: 25 },
  4: { totalTasks: 6, completedTasks: 2, progressPercent: 33 },
  5: { totalTasks: 5, completedTasks: 5, progressPercent: 100 },
};
