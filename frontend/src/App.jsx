import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import AppNavbar from './components/AppNavbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import HomePage from './pages/HomePage';
import StudentProfilePage from './pages/student/StudentProfilePage';
import EmployerProfilePage from './pages/employer/EmployerProfilePage';

const STUDENT = ['STUDENT'];
const EMPLOYER = ['EMPLOYER'];

/** Khai báo route theo vai trò. */
export default function App() {
  const guard = (roles, page) => <ProtectedRoute roles={roles}>{page}</ProtectedRoute>;
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

          <Route path="/student/profile" element={guard(STUDENT, <StudentProfilePage />)} />

          <Route path="/employer/profile" element={guard(EMPLOYER, <EmployerProfilePage />)} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}
