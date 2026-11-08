import { useEffect, useState } from 'react';
import adminApi from '../../api/adminApi';
import { errorMessage } from '../../api/axiosClient';
import PageHeader from '../../components/PageHeader';
import { categoryStyle } from '../../utils/ui';

/** Quản lý danh mục ngành nghề: thêm, sửa, ẩn / hiện (FR-21). */
export default function AdminCategoriesPage() {
  const [list, setList] = useState([]);
  const [form, setForm] = useState({ id: null, name: '', description: '' });
  const [msg, setMsg] = useState(null);

  const load = () => adminApi.categories().then(setList).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  useEffect(() => { load(); }, []);

  const run = async (action, okText) => {
    try {
      await action();
      setMsg({ ok: true, text: okText });
      setForm({ id: null, name: '', description: '' });
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const save = (e) => {
    e.preventDefault();
    if (!form.name.trim()) return setMsg({ ok: false, text: 'Tên ngành nghề không được để trống' });
    const data = { name: form.name, description: form.description };
    return form.id
      ? run(() => adminApi.updateCategory(form.id, data), 'Đã cập nhật ngành nghề')
      : run(() => adminApi.createCategory(data), 'Đã thêm ngành nghề');
  };

  const toggle = (c) => (c.active
    ? run(() => adminApi.hideCategory(c.id), `Đã ẩn “${c.name}”`)
    : run(() => adminApi.updateCategory(c.id, { name: c.name, description: c.description, active: true }), `Đã hiện “${c.name}”`));

  return (
    <>
    <PageHeader title="Danh mục ngành nghề" subtitle={`${list.filter((c) => c.active).length} ngành đang hiển thị / ${list.length} ngành`} />
    <div className="row g-4">
      <div className="col-lg-4">
        <form className="sv-card p-4" onSubmit={save}>
          <h6 className="fw-bold mb-3"><i className={`bi ${form.id ? 'bi-pencil-square' : 'bi-plus-circle'} text-primary`} /> {form.id ? 'Sửa ngành nghề' : 'Thêm ngành nghề'}</h6>
          <input className="form-control mb-2" placeholder="Tên ngành nghề" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <input className="form-control mb-2" placeholder="Mô tả" value={form.description || ''} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <div className="d-flex gap-2">
            <button className="btn btn-primary">{form.id ? 'Lưu' : 'Thêm'}</button>
            {form.id && <button type="button" className="btn btn-outline-secondary" onClick={() => setForm({ id: null, name: '', description: '' })}>Hủy</button>}
          </div>
        </form>
      </div>
      <div className="col-lg-8">
        {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
        <div className="sv-card table-responsive overflow-hidden">
          <table className="table sv-table align-middle mb-0">
            <thead><tr><th>Tên</th><th>Mô tả</th><th>Trạng thái</th><th /></tr></thead>
            <tbody>
              {list.map((c) => (
                <tr key={c.id}>
                  <td>
                    <span className="d-flex align-items-center gap-2">
                      <span className="cat-icon"><i className={`bi ${categoryStyle(c.name).icon}`} /></span>
                      <span className="fw-medium">{c.name}</span>
                    </span>
                  </td>
                  <td className="small">{c.description}</td>
                  <td>{c.active ? <span className="badge" style={{ background: '#dcfce7', color: '#15803d' }}>Hiện</span> : <span className="badge" style={{ background: '#f1f5f9', color: '#475569' }}>Ẩn</span>}</td>
                  <td className="text-end text-nowrap">
                    <button className="btn btn-sm btn-outline-primary me-1" onClick={() => setForm(c)}>Sửa</button>
                    <button className="btn btn-sm btn-outline-secondary" onClick={() => toggle(c)}>{c.active ? 'Ẩn' : 'Hiện'}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
    </>
  );
}
