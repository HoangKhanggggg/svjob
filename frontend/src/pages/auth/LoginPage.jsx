import { useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { errorMessage } from '../../api/axiosClient';
import { homeOf, useAuth } from '../../context/AuthContext';
import AuthLayout from '../../components/AuthLayout';

/** Đăng nhập (FR-02): sau khi đăng nhập chuyển về trang chính theo vai trò. */
export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [params] = useSearchParams();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (!form.email || !form.password) {
      setError('Vui lòng nhập email và mật khẩu');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const user = await login(form.email, form.password);
      navigate(location.state?.from || homeOf(user.role), { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Đăng nhập" subtitle="Chào bạn quay lại. Đăng nhập để ứng tuyển và theo dõi đơn của mình.">
      {params.get('expired') && <div className="alert alert-warning py-2">Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.</div>}
      {params.get('registered') && <div className="alert alert-success py-2">Đăng ký thành công, mời bạn đăng nhập.</div>}
      {error && <div className="alert alert-danger py-2">{error}</div>}
      <form onSubmit={submit} noValidate>
        <div className="mb-3">
          <label className="form-label">Email</label>
          <div className="input-group">
            <span className="input-group-text bg-white"><i className="bi bi-envelope" /></span>
            <input type="email" className="form-control border-start-0" placeholder="ban@vanlanguni.vn" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
        </div>
        <div className="mb-4">
          <label className="form-label">Mật khẩu</label>
          <div className="input-group">
            <span className="input-group-text bg-white"><i className="bi bi-lock" /></span>
            <input type="password" className="form-control border-start-0" placeholder="••••••••" value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
        </div>
        <button className="btn btn-primary w-100 py-2" disabled={loading}>{loading ? 'Đang đăng nhập...' : 'Đăng nhập'}</button>
      </form>
      <div className="text-center mt-3 small">Chưa có tài khoản? <Link to="/register" className="fw-semibold">Đăng ký miễn phí</Link></div>
    </AuthLayout>
  );
}
