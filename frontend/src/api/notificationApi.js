import axiosClient from './axiosClient';

const notificationApi = {
  list: () => axiosClient.get('/notifications'),
  read: (id) => axiosClient.patch(`/notifications/${id}/read`),
  readAll: () => axiosClient.patch('/notifications/read-all'),
};

export default notificationApi;
