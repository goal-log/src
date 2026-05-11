import request from './client'

export const getTasks = (params = {}) => {
  const qs = new URLSearchParams(params).toString()
  return request(`/api/tasks${qs ? `?${qs}` : ''}`)
}

export const createTask = (data) =>
  request('/api/tasks', { method: 'POST', body: JSON.stringify(data) })

export const completeTask = (id) =>
  request(`/api/tasks/${id}/complete`, { method: 'PATCH' })

export const deleteTask = (id) =>
  request(`/api/tasks/${id}`, { method: 'DELETE' })
