import api from './axios';

// GET /api/plans/{planId}/tasks → plan에 속한 task 목록
export const getPlanTasks = (planId) =>
  api.get(`/api/plans/${planId}/tasks`).then((res) => res.data.data);

// POST /api/plans/{planId}/tasks → { title } → 생성된 task 객체
export const createTask = (planId, title) =>
  api.post(`/api/plans/${planId}/tasks`, { title }).then((res) => res.data.data);

// PATCH /api/tasks/{id}/toggle → 완료 상태 토글 → 업데이트된 task 객체
export const toggleTask = (id) =>
  api.patch(`/api/tasks/${id}/toggle`).then((res) => res.data.data);

// DELETE /api/tasks/{id}
export const deleteTask = (id) =>
  api.delete(`/api/tasks/${id}`).then(() => null);
