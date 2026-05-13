import request from './client'

export const getDailyPlans = (params = {}) => {
  const qs = new URLSearchParams(params).toString()
  return request(`/api/daily-plans${qs ? `?${qs}` : ''}`)
}

export const createDailyPlan = (data) =>
  request('/api/daily-plans', { method: 'POST', body: JSON.stringify(data) })

export const completeDailyPlan = (id) =>
  request(`/api/daily-plans/${id}/complete`, { method: 'PATCH' })

export const deleteDailyPlan = (id) =>
  request(`/api/daily-plans/${id}`, { method: 'DELETE' })
