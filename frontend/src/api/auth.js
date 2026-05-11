<<<<<<< HEAD
import api from './axios';
=======
import axios from 'axios';

const api = axios.create({
  baseURL: '',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb

export const signup = (email, password) =>
  api.post('/api/auth/signup', { email, password });

export const login = (email, password) =>
<<<<<<< HEAD
  api.post('/api/auth/login', { email, password });
=======
  api.post('/api/auth/login', { email, password });
>>>>>>> 3a9def47da66385a4d243b883b64bbc008a99adb
