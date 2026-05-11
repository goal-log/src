import api from './axios';

export const signup = (email, password) =>
  api.post('/api/auth/signup', { email, password });

export const login = (email, password) =>
  api.post('/api/auth/login', { email, password });
