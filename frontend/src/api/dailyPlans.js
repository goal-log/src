import request from './client'

export const getPlanByDate = (date) =>
  request(`/api/plans?date=${date}`)

export const createPlan = (date, longTermGoalId = null) =>
  request('/api/plans', { method: 'POST', body: JSON.stringify({ date, longTermGoalId }) })

export const deletePlan = (id) =>
  request(`/api/plans/${id}`, { method: 'DELETE' })