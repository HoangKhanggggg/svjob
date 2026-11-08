import { createContext, useContext, useState } from 'react';
import authApi from '../api/authApi';

const AuthContext = createContext(null);

/** Lưu người dùng đang đăng nhập (token + thông tin) trong sessionStorage (mỗi tab một phiên, F5 không mất) . */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = sessionStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (email, password) => {
    const res = await authApi.login({ email, password });
    sessionStorage.setItem('token', res.token);
    sessionStorage.setItem('user', JSON.stringify(res.user));
    setUser(res.user);
    return res.user;
  };

  const logout = () => {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
    setUser(null);
  };

  const updateName = (fullName) => {
    const next = { ...user, fullName };
    sessionStorage.setItem('user', JSON.stringify(next));
    setUser(next);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, updateName }}>{children}</AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);

/** Trang chủ của từng vai trò sau khi đăng nhập. */
export function homeOf(role) {
  if (role === 'EMPLOYER') return '/employer/jobs';
  if (role === 'ADMIN') return '/admin/jobs';
  return '/';
}
