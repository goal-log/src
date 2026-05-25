import api from './axios';

// GET /api/plans?date=YYYY-MM-DD → plan 목록 (빈 배열 가능)
export const getPlanByDate = (date) =>
  api.get('/api/plans', { params: { date } })
    .then((res) => Array.isArray(res.data) ? res.data : [])
    .catch(() => []);

export const getAllPlans = () =>
  api.get('/api/plans/all').then((res) => res.data);

export const createPlan = (data) =>
  api.post('/api/plans', data).then((res) => res.data);

export const deletePlan = (id) =>
  api.delete(`/api/plans/${id}`).then(() => null);