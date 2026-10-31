import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import AppNavbar from './components/AppNavbar';
import Footer from './components/Footer';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import HomePage from './pages/HomePage';

/** Khai báo route theo vai trò. */
export default function App() {
  // Trang chủ có banner tràn màn hình nên không bọc container
  const fullWidth = useLocation().pathname === '/';
  return (
    <>
      <AppNavbar />
      <main className={fullWidth ? 'pb-4' : 'container py-4'}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}
