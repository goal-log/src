import request from './client'

export const getGoals = () =>
  request('/api/goals')

export const getGoal = (id) =>
  request(`/api/goals/${id}`)

export const createGoal = (data) =>
  request('/api/goals', { method: 'POST', body: JSON.stringify(data) })

export const updateGoal = (id, data) =>
  request(`/api/goals/${id}`, { method: 'PUT', body: JSON.stringify(data) })

export const deleteGoal = (id) =>
  request(`/api/goals/${id}`, { method: 'DELETE' })

export const getGoalProgress = (id) =>
  request(`/api/goals/${id}/progress`)