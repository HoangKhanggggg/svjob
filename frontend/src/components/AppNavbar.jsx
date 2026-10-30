import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Avatar from './Avatar';

const MENUS = {
  STUDENT: [['/', 'Việc làm'], ['/student/applications', 'Đơn ứng tuyển'], ['/student/profile', 'Hồ sơ & CV']],
  EMPLOYER: [['/employer/jobs', 'Quản lý tin đăng'], ['/employer/jobs/new', 'Đăng tin mới'], ['/employer/profile', 'Thông tin công ty']],
  ADMIN: [['/admin/jobs', 'Kiểm duyệt tin'], ['/admin/users', 'Người dùng'], ['/admin/categories', 'Ngành nghề']],
};
const ROLE_TEXT = { STUDENT: 'Sinh viên', EMPLOYER: 'Nhà tuyển dụng', ADMIN: 'Quản trị viên' };

/** Thanh menu thay đổi theo vai trò (FR-03: ẩn chức năng không thuộc vai trò). */
export default function AppNavbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const menus = user ? MENUS[user.role] : [['/', 'Việc làm']];

  const doLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-md sv-navbar sticky-top py-0">
      <div className="container">
        <Link className="navbar-brand logo me-4" to="/">SV<span>Job</span><small>Việc làm thêm cho sinh viên</small></Link>
        <button className="navbar-toggler border-0 px-0" type="button" data-bs-toggle="collapse" data-bs-target="#nav" aria-label="Menu">
          <span className="navbar-toggler-icon" />
        </button>
        <div className="collapse navbar-collapse" id="nav">
          <ul className="navbar-nav me-auto">
            {menus.map(([to, label]) => (
              <li className="nav-item" key={to}>
                <NavLink className="nav-link" to={to} end>{label}</NavLink>
              </li>
            ))}
          </ul>
          <div className="d-flex align-items-center gap-3 py-2 py-md-0">
            {user ? (
              <>
                <span className="nav-divider d-none d-md-block" />
                <span className="user-menu">
                  <Avatar name={user.fullName} size={32} round />
                  <span className="lh-sm">
                    {user.fullName}
                    <span className="d-block text-muted" style={{ fontSize: 11, fontWeight: 400 }}>{ROLE_TEXT[user.role]}</span>
                  </span>
                </span>
                <button className="btn btn-sm btn-link text-muted px-1" onClick={doLogout} title="Đăng xuất">
                  <i className="bi bi-box-arrow-right fs-5" /><span className="d-md-none ms-1">Đăng xuất</span>
                </button>
              </>
            ) : (
              <>
                <Link className="btn btn-outline-primary btn-sm px-3" to="/login">Đăng nhập</Link>
                <Link className="btn btn-primary btn-sm px-3" to="/register">Đăng ký</Link>
                <span className="nav-divider d-none d-lg-block" />
                <Link className="employer-link d-none d-lg-block lh-sm" to="/register">
                  <small>Bạn là nhà tuyển dụng?</small>Đăng tuyển ngay
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
