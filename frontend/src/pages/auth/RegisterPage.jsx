import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authApi from '../../api/authApi';
import { errorMessage, fieldErrors } from '../../api/axiosClient';
import AuthLayout from '../../components/AuthLayout';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Đăng ký SV hoặc NTD (FR-01). Kiểm tra dữ liệu phía client trước khi gọi API. */
export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ role: 'STUDENT', fullName: '', email: '', phone: '', password: '', confirm: '', companyName: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const validate = () => {
    const e = {};
    if (!form.fullName.trim()) e.fullName = 'Họ tên không được để trống';
    if (!EMAIL_RE.test(form.email)) e.email = 'Email không đúng định dạng';
    if (form.phone && !/^0\d{9,10}$/.test(form.phone)) e.phone = 'Số điện thoại không hợp lệ';
    if (form.password.length < 8) e.password = 'Mật khẩu tối thiểu 8 ký tự';
    if (form.confirm !== form.password) e.confirm = 'Mật khẩu nhập lại không khớp';
    if (form.role === 'EMPLOYER' && !form.companyName.trim()) e.companyName = 'Vui lòng nhập tên cửa hàng/công ty';
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    setErrors(e);
    if (Object.keys(e).length) return;
    setLoading(true);
    setError('');
    try {
      const { confirm, ...data } = form;
      await authApi.register(data);
      navigate('/login?registered=1');
    } catch (err) {
      setErrors(fieldErrors(err));
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const input = (key, label, type = 'text') => (
    <div className="mb-3">
      <label className="form-label">{label}</label>
      <input type={type} className={`form-control ${errors[key] ? 'is-invalid' : ''}`} value={form[key]} onChange={set(key)} />
      <div className="invalid-feedback">{errors[key]}</div>
    </div>
  );

  return (
    <AuthLayout title="Đăng ký tài khoản" subtitle="Miễn phí cho sinh viên và nhà tuyển dụng">
      {error && <div className="alert alert-danger py-2">{error}</div>}
      <form onSubmit={submit} noValidate>
        <label className="form-label">Bạn đăng ký với vai trò</label>
        <div className="row g-2 mb-3">
          {[['STUDENT', 'Sinh viên', 'Tìm việc làm thêm', 'bi-mortarboard'], ['EMPLOYER', 'Nhà tuyển dụng', 'Đăng tin tuyển người', 'bi-building']].map(([value, label, hint, icon]) => (
            <div className="col-6" key={value}>
              <button type="button" onClick={() => setForm({ ...form, role: value })} className={`role-option ${form.role === value ? 'active' : ''}`}>
                <i className={`bi ${icon}`} />
                <span className="lh-sm"><span className="fw-semibold d-block">{label}</span><span className="text-muted small">{hint}</span></span>
              </button>
            </div>
          ))}
        </div>
        <div className="row">
          <div className="col-sm-6">{input('fullName', form.role === 'EMPLOYER' ? 'Họ tên người đại diện' : 'Họ và tên')}</div>
          <div className="col-sm-6">{input('phone', 'Số điện thoại (không bắt buộc)')}</div>
        </div>
        {form.role === 'EMPLOYER' && input('companyName', 'Tên cửa hàng / công ty')}
        {input('email', 'Email', 'email')}
        <div className="row">
          <div className="col-sm-6">{input('password', 'Mật khẩu (tối thiểu 8 ký tự)', 'password')}</div>
          <div className="col-sm-6">{input('confirm', 'Nhập lại mật khẩu', 'password')}</div>
        </div>
        <button className="btn btn-primary w-100 py-2" disabled={loading}>{loading ? 'Đang xử lý...' : 'Đăng ký'}</button>
      </form>
      <div className="text-center mt-3 small">Đã có tài khoản? <Link to="/login" className="fw-semibold">Đăng nhập</Link></div>
    </AuthLayout>
  );
}
