<<<<<<< HEAD
import api from './axios';

export const getGoals = () =>
  api.get('/api/goals');

export const getGoal = (id) =>
  api.get(`/api/goals/${id}`);

export const createGoal = (data) =>
  api.post('/api/goals', data);

export const updateGoal = (id, data) =>
  api.put(`/api/goals/${id}`, data);

export const deleteGoal = (id) =>
  api.delete(`/api/goals/${id}`);

export const getGoalProgress = (id) =>
  api.get(`/api/goals/${id}/progress`);
=======
import request from './client'

export const getGoals = () => request('/api/goals')
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb
