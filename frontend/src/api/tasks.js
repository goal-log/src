import request from './client'

export const createTask = (planId, title) =>
  request(`/api/plans/${planId}/tasks`, { method: 'POST', body: JSON.stringify({ title }) })

export const toggleTask = (id) =>
  request(`/api/tasks/${id}/toggle`, { method: 'PATCH' })

export const deleteTask = (id) =>
  request(`/api/tasks/${id}`, { method: 'DELETE' })