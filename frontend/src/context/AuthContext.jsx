import { createContext, useContext, useState } from 'react';
import authApi from '../api/authApi';

const AuthContext = createContext(null);

/** Lưu người dùng đang đăng nhập (token + thông tin) trong localStorage để F5 không bị mất. */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (email, password) => {
    const res = await authApi.login({ email, password });
    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify(res.user));
    setUser(res.user);
    return res.user;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const updateName = (fullName) => {
    const next = { ...user, fullName };
    localStorage.setItem('user', JSON.stringify(next));
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
