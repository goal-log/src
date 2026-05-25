import api from './axios';

export const getPlanTasks = (planId) =>
  api.get(`/api/plans/${planId}/tasks`).then((res) => res.data);

export const createTask = (planId, title) =>
  api.post(`/api/plans/${planId}/tasks`, { title }).then((res) => res.data);

export const toggleTask = (id) =>
  api.patch(`/api/tasks/${id}/toggle`).then((res) => res.data);

export const deleteTask = (id) =>
  api.delete(`/api/tasks/${id}`).then(() => null);