import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import jobApi from '../../api/jobApi';
import { errorMessage, fieldErrors } from '../../api/axiosClient';

const EMPTY = { title: '', categoryId: '', description: '', salaryMin: '', salaryMax: '', salaryUnit: 'HOUR',
  location: '', workTime: '', quantity: 1, deadline: '' };

/** Đăng tin (FR-06) và sửa tin (FR-07) dùng chung 1 form. */
export default function JobFormPage() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [categories, setCategories] = useState([]);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    jobApi.categories().then(setCategories).catch(() => {});
    if (editing) {
      jobApi.detail(id).then((j) => setForm({
        title: j.title, categoryId: j.categoryId, description: j.description, salaryMin: j.salaryMin,
        salaryMax: j.salaryMax ?? '', salaryUnit: j.salaryUnit, location: j.location, workTime: j.workTime,
        quantity: j.quantity, deadline: j.deadline,
      })).catch((err) => setError(errorMessage(err)));
    }
  }, [id, editing]);

  const validate = () => {
    const e = {};
    ['title', 'description', 'location', 'workTime'].forEach((k) => { if (!String(form[k]).trim()) e[k] = 'Không được để trống'; });
    if (!form.categoryId) e.categoryId = 'Vui lòng chọn ngành nghề';
    if (form.salaryMin === '' || Number(form.salaryMin) < 0) e.salaryMin = 'Nhập mức lương hợp lệ';
    if (form.salaryMax !== '' && Number(form.salaryMax) < Number(form.salaryMin)) e.salaryMax = 'Phải lớn hơn hoặc bằng lương từ';
    if (Number(form.quantity) < 1) e.quantity = 'Cần tuyển ít nhất 1 người';
    if (!form.deadline || new Date(form.deadline) <= new Date()) e.deadline = 'Hạn nộp phải sau ngày hôm nay';
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    setErrors(e);
    if (Object.keys(e).length) return;
    setSaving(true);
    setError('');
    const data = { ...form, categoryId: Number(form.categoryId), salaryMin: Number(form.salaryMin),
      salaryMax: form.salaryMax === '' ? null : Number(form.salaryMax), quantity: Number(form.quantity) };
    try {
      if (editing) await jobApi.update(id, data);
      else await jobApi.create(data);
      navigate('/employer/jobs');
    } catch (err) {
      setErrors(fieldErrors(err));
      setError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const invalid = (k) => (errors[k] ? 'is-invalid' : '');
  const feedback = (k) => <div className="invalid-feedback">{errors[k]}</div>;

  return (
    <div className="row justify-content-center">
      <div className="col-lg-9">
        <form className="sv-card p-4" onSubmit={submit} noValidate>
          <div className="d-flex align-items-center gap-3 mb-4 pb-3 border-bottom">
            <div>
              <h4 className="fw-bold mb-0">{editing ? 'Sửa tin tuyển dụng' : 'Đăng tin tuyển dụng'}</h4>
              <div className="text-muted small">Tin rõ ràng về lương và giờ làm sẽ nhận được nhiều ứng viên hơn</div>
            </div>
          </div>
          {error && <div className="alert alert-danger py-2">{error}</div>}
          <div className="mb-3">
            <label className="form-label">Tiêu đề *</label>
            <input className={`form-control ${invalid('title')}`} value={form.title} onChange={set('title')} placeholder="VD: Nhân viên phục vụ quán cà phê" />
            {feedback('title')}
          </div>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Ngành nghề *</label>
              <select className={`form-select ${invalid('categoryId')}`} value={form.categoryId} onChange={set('categoryId')}>
                <option value="">-- Chọn --</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              {feedback('categoryId')}
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Địa điểm *</label>
              <input className={`form-control ${invalid('location')}`} value={form.location} onChange={set('location')} placeholder="Quận/huyện hoặc Online" />
              {feedback('location')}
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Lương từ (đ) *</label>
              <input type="number" min="0" className={`form-control ${invalid('salaryMin')}`} value={form.salaryMin} onChange={set('salaryMin')} />
              {feedback('salaryMin')}
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Lương đến (đ)</label>
              <input type="number" min="0" className={`form-control ${invalid('salaryMax')}`} value={form.salaryMax} onChange={set('salaryMax')} />
              {feedback('salaryMax')}
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Đơn vị</label>
              <select className="form-select" value={form.salaryUnit} onChange={set('salaryUnit')}>
                <option value="HOUR">Theo giờ</option>
                <option value="SHIFT">Theo ca / buổi</option>
                <option value="MONTH">Theo tháng</option>
              </select>
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Thời gian làm việc *</label>
              <input className={`form-control ${invalid('workTime')}`} value={form.workTime} onChange={set('workTime')} placeholder="VD: Ca tối 18h-22h" />
              {feedback('workTime')}
            </div>
            <div className="col-md-3 mb-3">
              <label className="form-label">Số lượng *</label>
              <input type="number" min="1" className={`form-control ${invalid('quantity')}`} value={form.quantity} onChange={set('quantity')} />
              {feedback('quantity')}
            </div>
            <div className="col-md-3 mb-3">
              <label className="form-label">Hạn nộp *</label>
              <input type="date" className={`form-control ${invalid('deadline')}`} value={form.deadline} onChange={set('deadline')} />
              {feedback('deadline')}
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label">Mô tả công việc và yêu cầu *</label>
            <textarea rows={5} className={`form-control ${invalid('description')}`} value={form.description} onChange={set('description')} />
            {feedback('description')}
          </div>
          <div className="d-flex gap-2">
            <button className="btn btn-primary px-4" disabled={saving}>{saving ? 'Đang lưu...' : editing ? 'Lưu thay đổi' : 'Đăng tin'}</button>
            <button type="button" className="btn btn-outline-secondary" onClick={() => navigate(-1)}>Hủy</button>
          </div>
        </form>
      </div>
    </div>
  );
}
