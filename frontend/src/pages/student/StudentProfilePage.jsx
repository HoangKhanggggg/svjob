import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import profileApi from '../../api/profileApi';
import { errorMessage, fieldErrors } from '../../api/axiosClient';
import { useAuth } from '../../context/AuthContext';
import Stars from '../../components/Stars';
import { openBlob } from '../../utils/format';
import Avatar from '../../components/Avatar';

/** Hồ sơ sinh viên + tải CV PDF ≤ 5 MB (FR-04). */
export default function StudentProfilePage() {
  const { updateName } = useAuth();
  const [form, setForm] = useState(null);
  const [info, setInfo] = useState(null);
  const [errors, setErrors] = useState({});
  const [msg, setMsg] = useState(null);

  const fill = (p) => {
    setInfo(p);
    setForm({ fullName: p.fullName || '', phone: p.phone || '', school: p.school || '', major: p.major || '',
      yearOfStudy: p.yearOfStudy || '', skills: p.skills || '', bio: p.bio || '' });
  };

  useEffect(() => {
    profileApi.student().then(fill).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  }, []);

  const save = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      const p = await profileApi.updateStudent({ ...form, yearOfStudy: form.yearOfStudy || null });
      fill(p);
      updateName(p.fullName);
      setMsg({ ok: true, text: 'Đã lưu hồ sơ' });
    } catch (err) {
      setErrors(fieldErrors(err));
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const upload = async (e) => {
    const file = e.target.files[0];
    e.target.value = '';
    if (!file) return;
    if (!file.name.toLowerCase().endsWith('.pdf')) return setMsg({ ok: false, text: 'Chỉ chấp nhận file PDF' });
    if (file.size > 5 * 1024 * 1024) return setMsg({ ok: false, text: 'File tối đa 5 MB' });
    try {
      fill(await profileApi.uploadCv(file));
      setMsg({ ok: true, text: 'Đã tải CV lên' });
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const viewCv = async () => openBlob(await profileApi.myCv());

  if (!form) return <div className="text-center text-muted">{msg?.text || 'Đang tải...'}</div>;
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const field = (k, label, props = {}) => (
    <div className="mb-3">
      <label className="form-label">{label}</label>
      <input className={`form-control ${errors[k] ? 'is-invalid' : ''}`} value={form[k]} onChange={set(k)} {...props} />
      <div className="invalid-feedback">{errors[k]}</div>
    </div>
  );

  return (
    <div className="row g-4">
      <div className="col-lg-8">
        <form className="sv-card p-4" onSubmit={save} noValidate>
          <div className="d-flex gap-3 align-items-center mb-4 pb-3 border-bottom">
            <Avatar name={info.fullName} size={64} round />
            <div>
              <h4 className="fw-bold mb-0">{info.fullName}</h4>
              <div className="text-muted small">{[info.major, info.school].filter(Boolean).join(' · ') || 'Hoàn thiện hồ sơ để nhà tuyển dụng hiểu bạn hơn'}</div>
            </div>
          </div>
          {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
          <div className="row">
            <div className="col-md-6">{field('fullName', 'Họ và tên')}</div>
            <div className="col-md-6">{field('phone', 'Số điện thoại')}</div>
            <div className="col-md-6">{field('school', 'Trường')}</div>
            <div className="col-md-4">{field('major', 'Ngành')}</div>
            <div className="col-md-2">{field('yearOfStudy', 'Năm', { type: 'number', min: 1, max: 6 })}</div>
          </div>
          {field('skills', 'Kỹ năng (cách nhau dấu phẩy)')}
          <div className="mb-3">
            <label className="form-label">Giới thiệu bản thân</label>
            <textarea className="form-control" rows={4} value={form.bio} onChange={set('bio')} />
          </div>
          <button className="btn btn-primary px-4"><i className="bi bi-check2" /> Lưu hồ sơ</button>
        </form>
      </div>
      <div className="col-lg-4">
        <div className="sv-card p-4 mb-3">
          <h6 className="fw-bold"><i className="bi bi-file-earmark-pdf text-danger" /> CV của tôi</h6>
          {info.hasCv ? (
            <div className="d-flex align-items-center justify-content-between p-2 rounded-3 bg-light mb-2">
              <span className="small"><i className="bi bi-check-circle-fill text-success" /> Đã có CV</span>
              <button className="btn btn-sm btn-outline-primary" onClick={viewCv}>Xem CV</button>
            </div>
          ) : <div className="text-muted small mb-2">Chưa tải CV. Có CV sẵn giúp ứng tuyển nhanh hơn.</div>}
          <div className="text-muted mb-1" style={{ fontSize: 12 }}>Tải lên CV mới (PDF ≤ 5 MB)</div>
          <input type="file" accept="application/pdf" className="form-control form-control-sm" onChange={upload} />
        </div>
        <div className="sv-card p-4">
          <h6 className="fw-bold"><i className="bi bi-star text-warning" /> Đánh giá từ nhà tuyển dụng</h6>
          <Stars value={info.avgRating} count={info.reviewCount} />
          {info.reviewCount > 0 && <Link className="small d-block mt-1" to={`/reviews/${info.userId}`}>Xem chi tiết</Link>}
        </div>
      </div>
    </div>
  );
}
