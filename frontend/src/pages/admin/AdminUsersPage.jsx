import { useEffect, useState } from 'react';
import adminApi from '../../api/adminApi';
import { errorMessage } from '../../api/axiosClient';
import Pagination from '../../components/Pagination';
import { formatDate } from '../../utils/format';
import PageHeader from '../../components/PageHeader';
import Avatar from '../../components/Avatar';

const ROLE_COLORS = { STUDENT: ['#e7f0fb', '#1f5fa8'], EMPLOYER: ['#e8f6ee', '#00843f'], ADMIN: ['#fdecea', '#b42318'] };

const ROLES = { STUDENT: 'Sinh viên', EMPLOYER: 'Nhà tuyển dụng', ADMIN: 'Quản trị viên' };

/** Quản lý người dùng: tìm kiếm, khóa / mở khóa (FR-20). */
export default function AdminUsersPage() {
  const [filter, setFilter] = useState({ keyword: '', role: '', status: '' });
  const [query, setQuery] = useState(filter);
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [msg, setMsg] = useState(null);

  const load = () => {
    const params = { page };
    Object.entries(query).forEach(([k, v]) => { if (v) params[k] = v; });
    adminApi.users(params).then(setData).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  };
  useEffect(load, [query, page]);

  const toggle = async (u) => {
    const next = u.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    if (next === 'LOCKED' && !window.confirm(`Khóa tài khoản ${u.email}? Người này sẽ không đăng nhập được.`)) return;
    try {
      await adminApi.setUserStatus(u.id, next);
      setMsg({ ok: true, text: `${next === 'LOCKED' ? 'Đã khóa' : 'Đã mở khóa'} ${u.email}` });
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const set = (k) => (e) => setFilter({ ...filter, [k]: e.target.value });
  return (
    <>
      <PageHeader title="Quản lý người dùng" subtitle={data ? `${data.totalElements} tài khoản phù hợp bộ lọc` : 'Tìm kiếm, khóa / mở khóa tài khoản'} />
      <form className="sv-card p-3 row g-2 mx-0 mb-3" onSubmit={(e) => { e.preventDefault(); setPage(0); setQuery(filter); }}>
        <div className="col-md-5"><input className="form-control" placeholder="Email hoặc họ tên" value={filter.keyword} onChange={set('keyword')} /></div>
        <div className="col-md-3">
          <select className="form-select" value={filter.role} onChange={set('role')}>
            <option value="">Mọi vai trò</option>
            {Object.entries(ROLES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </div>
        <div className="col-md-2">
          <select className="form-select" value={filter.status} onChange={set('status')}>
            <option value="">Mọi trạng thái</option>
            <option value="ACTIVE">Hoạt động</option>
            <option value="LOCKED">Bị khóa</option>
          </select>
        </div>
        <div className="col-md-2 d-grid"><button className="btn btn-primary"><i className="bi bi-search" /> Tìm</button></div>
      </form>
      {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
      {data && (
        <div className="sv-card table-responsive overflow-hidden">
          <table className="table sv-table align-middle mb-0">
            <thead><tr><th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Ngày tạo</th><th>Trạng thái</th><th /></tr></thead>
            <tbody>
              {data.items.map((u) => (
                <tr key={u.id}>
                  <td><span className="d-flex align-items-center gap-2"><Avatar name={u.fullName} size={34} round /><span className="fw-medium">{u.fullName}</span></span></td>
                  <td className="small">{u.email}</td>
                  <td><span className="badge" style={{ background: ROLE_COLORS[u.role][0], color: ROLE_COLORS[u.role][1] }}>{ROLES[u.role]}</span></td>
                  <td>{formatDate(u.createdAt)}</td>
                  <td>{u.status === 'ACTIVE'
                    ? <span className="badge" style={{ background: '#dcfce7', color: '#15803d' }}><i className="bi bi-circle-fill me-1" style={{ fontSize: 7 }} />Hoạt động</span>
                    : <span className="badge" style={{ background: '#fee2e2', color: '#b91c1c' }}><i className="bi bi-lock-fill me-1" />Bị khóa</span>}</td>
                  <td className="text-end">
                    {u.role !== 'ADMIN' && (
                      <button className={`btn btn-sm ${u.status === 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}`} onClick={() => toggle(u)}>
                        <i className={`bi ${u.status === 'ACTIVE' ? 'bi-lock' : 'bi-unlock'}`} /> {u.status === 'ACTIVE' ? 'Khóa' : 'Mở khóa'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  );
}
