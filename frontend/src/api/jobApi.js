import axiosClient from './axiosClient';

const jobApi = {
  categories: () => axiosClient.get('/categories'),
  stats: () => axiosClient.get('/stats'),
  search: (params) => axiosClient.get('/jobs', { params }),
  detail: (id) => axiosClient.get(`/jobs/${id}`),
  create: (data) => axiosClient.post('/jobs', data),
  myJobs: () => axiosClient.get('/employers/me/jobs'),
};

export default jobApi;
