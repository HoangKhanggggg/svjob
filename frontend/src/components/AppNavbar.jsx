import { Link, NavLink } from 'react-router-dom';

/** Thanh menu chung. Buổi 6 sẽ thêm đăng nhập và menu theo vai trò. */
export default function AppNavbar() {
  return (
    <nav className="navbar navbar-expand-md sv-navbar sticky-top py-0">
      <div className="container">
        <Link className="navbar-brand logo me-4" to="/">SV<span>Job</span><small>Việc làm thêm cho sinh viên</small></Link>
        <button className="navbar-toggler border-0 px-0" type="button" data-bs-toggle="collapse" data-bs-target="#nav" aria-label="Menu">
          <span className="navbar-toggler-icon" />
        </button>
        <div className="collapse navbar-collapse" id="nav">
          <ul className="navbar-nav me-auto">
            <li className="nav-item"><NavLink className="nav-link" to="/" end>Việc làm</NavLink></li>
          </ul>
        </div>
      </div>
    </nav>
  );
}
