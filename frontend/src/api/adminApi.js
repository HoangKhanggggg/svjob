import axiosClient from './axiosClient';

const adminApi = {
  users: (params) => axiosClient.get('/admin/users', { params }),
  setUserStatus: (id, status) => axiosClient.patch(`/admin/users/${id}/status`, { status }),
  jobs: (params) => axiosClient.get('/admin/jobs', { params }),
  hideJob: (id, hidden, reason) => axiosClient.patch(`/admin/jobs/${id}/hide`, { hidden, reason }),
  categories: () => axiosClient.get('/admin/categories'),
  createCategory: (data) => axiosClient.post('/admin/categories', data),
  updateCategory: (id, data) => axiosClient.put(`/admin/categories/${id}`, data),
  hideCategory: (id) => axiosClient.delete(`/admin/categories/${id}`),
};

export default adminApi;
