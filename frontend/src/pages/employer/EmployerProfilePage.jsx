import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import profileApi from '../../api/profileApi';
import { errorMessage, fieldErrors } from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import Stars from '../../components/Stars';
import Avatar from '../../components/Avatar';

/** Hồ sơ nhà tuyển dụng (FR-05). */
export default function EmployerProfilePage() {
  const { updateName } = useAuth();
  const [form, setForm] = useState(null);
  const [info, setInfo] = useState(null);
  const [errors, setErrors] = useState({});
  const [msg, setMsg] = useState(null);

  const fill = (p) => {
    setInfo(p);
    setForm({ fullName: p.fullName || '', companyName: p.companyName || '', address: p.address || '',
      description: p.description || '', contactPhone: p.contactPhone || '' });
  };

  useEffect(() => {
    profileApi.employer().then(fill).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  }, []);

  const save = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      const p = await profileApi.updateEmployer(form);
      fill(p);
      updateName(p.fullName);
      setMsg({ ok: true, text: 'Đã lưu hồ sơ' });
    } catch (err) {
      setErrors(fieldErrors(err));
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  if (!form) return <div className="text-center text-muted">{msg?.text || 'Đang tải...'}</div>;
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const field = (k, label) => (
    <div className="mb-3">
      <label className="form-label">{label}</label>
      <input className={`form-control ${errors[k] ? 'is-invalid' : ''}`} value={form[k]} onChange={set(k)} />
      <div className="invalid-feedback">{errors[k]}</div>
    </div>
  );

  return (
    <div className="row justify-content-center">
      <div className="col-lg-8">
        <form className="sv-card p-4" onSubmit={save} noValidate>
          <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4 pb-3 border-bottom">
            <div className="d-flex gap-3 align-items-center">
              <Avatar name={info.companyName} size={64} />
              <div>
                <h4 className="fw-bold mb-0">{info.companyName}</h4>
                <Stars value={info.avgRating} count={info.reviewCount} />
              </div>
            </div>
            <Link className="btn btn-sm btn-outline-primary" to={`/employers/${info.userId}`}><i className="bi bi-box-arrow-up-right" /> Xem trang công khai</Link>
          </div>
          {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
          <div className="row">
            <div className="col-md-6">{field('companyName', 'Tên cửa hàng / công ty')}</div>
            <div className="col-md-6">{field('fullName', 'Người đại diện')}</div>
            <div className="col-md-8">{field('address', 'Địa chỉ')}</div>
            <div className="col-md-4">{field('contactPhone', 'SĐT liên hệ')}</div>
          </div>
          <div className="mb-3">
            <label className="form-label">Giới thiệu</label>
            <textarea className="form-control" rows={4} value={form.description} onChange={set('description')} />
          </div>
          <button className="btn btn-primary px-4"><i className="bi bi-check2" /> Lưu hồ sơ</button>
        </form>
      </div>
    </div>
  );
}
