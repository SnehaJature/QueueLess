import api from './axiosInstance';

export const getQueuesByBusiness = (businessId) => api.get(`/api/queues/business/${businessId}`);
export const getQueue = (queueId) => api.get(`/api/queues/${queueId}`);
export const createQueue = (data) => api.post('/api/queues', data);
export const openQueue = (queueId) => api.post(`/api/queues/${queueId}/open`);
export const pauseQueue = (queueId) => api.post(`/api/queues/${queueId}/pause`);
export const closeQueue = (queueId) => api.post(`/api/queues/${queueId}/close`);
export const getQueueStats = (queueId) => api.get(`/api/queues/${queueId}/stats`);

export const joinQueue = (queueId) => api.post(`/api/queues/${queueId}/join`);
export const getMyToken = () => api.get('/api/queues/my-token');
export const getToken = (tokenId) => api.get(`/api/queues/tokens/${tokenId}`);
export const cancelToken = (tokenId) => api.delete(`/api/queues/tokens/${tokenId}`);

export const callNext = (queueId) => api.post(`/api/queues/${queueId}/next`);
export const completeToken = (tokenId) => api.post(`/api/queues/tokens/${tokenId}/complete`);
export const skipToken = (tokenId) => api.post(`/api/queues/tokens/${tokenId}/skip`);
export const getWaitingTokens = (queueId) => api.get(`/api/queues/${queueId}/waiting`);
export const getAlternatives = (queueId) => api.get(`/api/queues/${queueId}/alternatives`);
