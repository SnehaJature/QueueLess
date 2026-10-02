import api from './axiosInstance';

export const register = (data) => api.post('/api/users/register', data);
export const login = (data) => api.post('/api/users/login', data);
export const getProfile = () => api.get('/api/users/profile');
export const updateProfile = (id, data) => api.put(`/api/users/${id}`, data);
