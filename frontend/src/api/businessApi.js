import api from './axiosInstance';

export const getBusinesses = (params) => api.get('/api/businesses', { params });
export const getBusinessById = (id) => api.get(`/api/businesses/${id}`);
export const createBusiness = (data) => api.post('/api/businesses', data);
export const updateBusiness = (id, data) => api.put(`/api/businesses/${id}`, data);
export const deleteBusiness = (id) => api.delete(`/api/businesses/${id}`);
export const openBusiness = (id) => api.patch(`/api/businesses/${id}/open`);
export const closeBusiness = (id) => api.patch(`/api/businesses/${id}/close`);
export const getServices = (businessId) => api.get(`/api/businesses/${businessId}/services`);
export const addService = (businessId, data) => api.post(`/api/businesses/${businessId}/services`, data);
export const updateService = (serviceId, data) => api.put(`/api/businesses/services/${serviceId}`, data);
export const deleteService = (serviceId) => api.delete(`/api/businesses/services/${serviceId}`);
