import api from './axios';

// GET /api/plans?date=YYYY-MM-DD → 단일 plan 객체, 없으면 null (404)
export const getPlanByDate = (date) =>
  api.get('/api/plans', { params: { date } })
    .then((res) => res.data.data)
    .catch((err) => {
      if (err.response?.status === 404) return null;
      throw err;
    });

// GET /api/plans/all → 전체 plan 목록
export const getAllPlans = () =>
  api.get('/api/plans/all').then((res) => res.data.data);

// POST /api/plans → { date, longTermGoalId? } → plan 객체
// 같은 날짜에 이미 plan이 있으면 기존 plan 반환 (idempotent)
export const createPlan = (data) =>
  api.post('/api/plans', data).then((res) => res.data.data);

// DELETE /api/plans/{id}
export const deletePlan = (id) =>
  api.delete(`/api/plans/${id}`).then(() => null);
