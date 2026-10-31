import axiosClient from './axiosClient';

const profileApi = {
  student: () => axiosClient.get('/students/me/profile'),
  updateStudent: (data) => axiosClient.put('/students/me/profile', data),
  uploadCv: (file) => {
    const form = new FormData();
    form.append('file', file);
    return axiosClient.post('/students/me/cv', form);
  },
  myCv: () => axiosClient.get('/students/me/cv', { responseType: 'blob' }),
  employer: () => axiosClient.get('/employers/me/profile'),
  updateEmployer: (data) => axiosClient.put('/employers/me/profile', data),
};

export default profileApi;
