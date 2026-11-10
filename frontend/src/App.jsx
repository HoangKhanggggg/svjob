import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import AppNavbar from './components/AppNavbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import HomePage from './pages/HomePage';
import JobDetailPage from './pages/JobDetailPage';
import EmployerPublicPage from './pages/EmployerPublicPage';
import ReviewsPage from './pages/ReviewsPage';
import StudentProfilePage from './pages/student/StudentProfilePage';
import MyApplicationsPage from './pages/student/MyApplicationsPage';
import EmployerProfilePage from './pages/employer/EmployerProfilePage';
import MyJobsPage from './pages/employer/MyJobsPage';
import JobFormPage from './pages/employer/JobFormPage';
import ApplicantsPage from './pages/employer/ApplicantsPage';
import AdminJobsPage from './pages/admin/AdminJobsPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage';

const STUDENT = ['STUDENT'];
const EMPLOYER = ['EMPLOYER'];
const ADMIN = ['ADMIN'];

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
          <Route path="/jobs/:id" element={<JobDetailPage />} />
          <Route path="/employers/:id" element={<EmployerPublicPage />} />
          <Route path="/reviews/:id" element={<ReviewsPage />} />

          <Route path="/student/profile" element={guard(STUDENT, <StudentProfilePage />)} />
          <Route path="/student/applications" element={guard(STUDENT, <MyApplicationsPage />)} />

          <Route path="/employer/profile" element={guard(EMPLOYER, <EmployerProfilePage />)} />
          <Route path="/employer/jobs" element={guard(EMPLOYER, <MyJobsPage />)} />
          <Route path="/employer/jobs/new" element={guard(EMPLOYER, <JobFormPage />)} />
          <Route path="/employer/jobs/:id/edit" element={guard(EMPLOYER, <JobFormPage />)} />
          <Route path="/employer/jobs/:id/applicants" element={guard(EMPLOYER, <ApplicantsPage />)} />

          <Route path="/admin/jobs" element={guard(ADMIN, <AdminJobsPage />)} />
          <Route path="/admin/users" element={guard(ADMIN, <AdminUsersPage />)} />
          <Route path="/admin/categories" element={guard(ADMIN, <AdminCategoriesPage />)} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}