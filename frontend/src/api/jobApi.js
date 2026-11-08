import axiosClient from './axiosClient';

const jobApi = {
  categories: () => axiosClient.get('/categories'),
  stats: () => axiosClient.get('/stats'),
  search: (params) => axiosClient.get('/jobs', { params }),
  detail: (id) => axiosClient.get(`/jobs/${id}`),
  create: (data) => axiosClient.post('/jobs', data),
  update: (id, data) => axiosClient.put(`/jobs/${id}`, data),
  changeStatus: (id, status) => axiosClient.patch(`/jobs/${id}/status`, { status }),
  remove: (id) => axiosClient.delete(`/jobs/${id}`),
  myJobs: () => axiosClient.get('/employers/me/jobs'),
};

export default jobApi;
