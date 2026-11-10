import axiosClient from './axiosClient';

const applicationApi = {
  // Gửi dạng multipart vì có thể kèm file CV
  apply: (jobId, { cv, coverLetter, useProfileCv }) => {
    const form = new FormData();
    if (cv) form.append('cv', cv);
    if (coverLetter) form.append('coverLetter', coverLetter);
    form.append('useProfileCv', useProfileCv ? 'true' : 'false');
    return axiosClient.post(`/jobs/${jobId}/applications`, form);
  },
  mine: () => axiosClient.get('/students/me/applications'),
  withdraw: (id) => axiosClient.patch(`/applications/${id}/withdraw`),
  applicants: (jobId) => axiosClient.get(`/jobs/${jobId}/applications`),
  decide: (id, status) => axiosClient.patch(`/applications/${id}/status`, { status }),
  cv: (id) => axiosClient.get(`/applications/${id}/cv`, { responseType: 'blob' }),
};

export default applicationApi;
