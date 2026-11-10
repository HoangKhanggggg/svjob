import axiosClient from './axiosClient';

const reviewApi = {
  create: (jobId, data) => axiosClient.post(`/jobs/${jobId}/reviews`, data),
  ofUser: (userId) => axiosClient.get(`/users/${userId}/reviews`),
};

export default reviewApi;
