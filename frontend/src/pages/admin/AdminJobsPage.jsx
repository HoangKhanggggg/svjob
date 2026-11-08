import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import adminApi from '../../api/adminApi';
import { errorMessage } from '../../api/axiosClient';
import Pagination from '../../components/Pagination';
import { JobStatusBadge } from '../../components/StatusBadge';
import { JOB_STATUS, formatDate } from '../../utils/format';
import PageHeader from '../../components/PageHeader';
import Avatar from '../../components/Avatar';

/** Kiểm duyệt sau: Admin xem mọi tin, ẩn tin vi phạm kèm lý do hoặc bỏ ẩn (FR-08). */
export default function AdminJobsPage() {
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [query, setQuery] = useState({ keyword: '', status: '' });
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [msg, setMsg] = useState(null);
  const [hiding, setHiding] = useState(null);
  const [reason, setReason] = useState('');

  const load = () => {
    const params = { page };
    if (query.keyword) params.keyword = query.keyword;
    if (query.status) params.status = query.status;
    adminApi.jobs(params).then(setData).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  };
  useEffect(load, [query, page]);

  const doHide = async (e) => {
    e.preventDefault();
    if (!reason.trim()) return;
    try {
      await adminApi.hideJob(hiding.id, true, reason);
      setMsg({ ok: true, text: `Đã ẩn tin “${hiding.title}”` });
      setHiding(null);
      setReason('');
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const unhide = async (j) => {
    try {
      await adminApi.hideJob(j.id, false, null);
      setMsg({ ok: true, text: `Đã hiển thị lại tin “${j.title}”` });
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  return (
    <>
      <PageHeader title="Kiểm duyệt tin tuyển dụng" subtitle={data ? `${data.totalElements} tin phù hợp bộ lọc` : 'Xem toàn bộ tin, ẩn tin vi phạm kèm lý do'} />
      <form className="sv-card p-3 row g-2 mx-0 mb-3" onSubmit={(e) => { e.preventDefault(); setPage(0); setQuery({ keyword, status }); }}>
        <div className="col-md-6"><input className="form-control" placeholder="Tìm theo tiêu đề, mô tả" value={keyword} onChange={(e) => setKeyword(e.target.value)} /></div>
        <div className="col-md-4">
          <select className="form-select" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">Tất cả trạng thái</option>
            {Object.entries(JOB_STATUS).map(([k, v]) => <option key={k} value={k}>{v.text}</option>)}
          </select>
        </div>
        <div className="col-md-2 d-grid"><button className="btn btn-primary"><i className="bi bi-funnel" /> Lọc</button></div>
      </form>
      {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
      {data && (
        <div className="sv-card table-responsive overflow-hidden">
          <table className="table sv-table align-middle mb-0">
            <thead><tr><th>Tin</th><th>Nhà tuyển dụng</th><th>Ngày đăng</th><th>Trạng thái</th><th /></tr></thead>
            <tbody>
              {data.items.map((j) => (
                <tr key={j.id}>
                  <td><Link to={`/jobs/${j.id}`} className="fw-medium text-body">{j.title}</Link>
                    <div className="small text-muted">{j.categoryName} · {j.location}</div>
                    {j.hiddenReason && <div className="small text-danger"><i className="bi bi-eye-slash" /> Lý do ẩn: {j.hiddenReason}</div>}</td>
                  <td><span className="d-flex align-items-center gap-2 small"><Avatar name={j.companyName} size={30} />{j.companyName}</span></td>
                  <td>{formatDate(j.createdAt)}</td>
                  <td><JobStatusBadge status={j.status} /></td>
                  <td className="text-end text-nowrap">
                    {j.status === 'HIDDEN'
                      ? <button className="btn btn-sm btn-outline-success" onClick={() => unhide(j)}><i className="bi bi-eye" /> Bỏ ẩn</button>
                      : <button className="btn btn-sm btn-outline-danger" onClick={() => setHiding(j)}><i className="bi bi-eye-slash" /> Ẩn tin</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}

      {hiding && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,.4)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <form className="modal-content" onSubmit={doHide}>
              <div className="modal-header"><h5 className="modal-title">Ẩn tin “{hiding.title}”</h5>
                <button type="button" className="btn-close" onClick={() => setHiding(null)} /></div>
              <div className="modal-body">
                <label className="form-label">Lý do ẩn (gửi cho nhà tuyển dụng) *</label>
                <textarea className="form-control" rows={3} maxLength={255} required value={reason} onChange={(e) => setReason(e.target.value)} />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-outline-secondary" onClick={() => setHiding(null)}>Hủy</button>
                <button className="btn btn-danger" disabled={!reason.trim()}>Ẩn tin</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
