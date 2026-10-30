import axios from 'axios';

// Một chỗ duy nhất cấu hình gọi API: địa chỉ backend, gắn token, xử lý token hết hạn
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

axiosClient.interceptors.response.use(
  (res) => res.data,
  (error) => {
    // Token hết hạn (sau 60 phút) hoặc tài khoản bị khóa -> đăng xuất và về trang đăng nhập (TC-06)
    if (error.response?.status === 401 && localStorage.getItem('token')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login?expired=1';
    }
    return Promise.reject(error);
  },
);

/** Lấy câu thông báo lỗi từ backend để hiện cho người dùng. */
export function errorMessage(error) {
  return error.response?.data?.message || 'Không kết nối được máy chủ, vui lòng thử lại';
}

/** Lỗi theo từng trường (400) để hiện dưới ô nhập. */
export function fieldErrors(error) {
  return error.response?.data?.errors || {};
}

export default axiosClient;
