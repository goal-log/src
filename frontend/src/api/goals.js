import request from './client'

export const getGoals = () => request('/api/goals')
