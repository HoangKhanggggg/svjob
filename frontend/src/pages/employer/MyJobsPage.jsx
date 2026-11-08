import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import jobApi from '../../api/jobApi';
import { errorMessage } from '../../api/axiosClient';
import { JobStatusBadge } from '../../components/StatusBadge';
import { formatDate } from '../../utils/format';
import PageHeader from '../../components/PageHeader';
import StatCard from '../../components/StatCard';
import EmptyState from '../../components/EmptyState';
import { categoryStyle } from '../../utils/ui';

/** Nút chuyển trạng thái hợp lệ theo BR-09 (giống bảng ALLOWED ở JobService phía backend). */
const NEXT = {
  OPEN: [['FILLED', 'Đã nhận đủ người', 'primary'], ['CLOSED', 'Đóng tin', 'outline-dark']],
  FILLED: [['COMPLETED', 'Xác nhận hoàn thành', 'success'], ['CLOSED', 'Đóng tin', 'outline-dark']],
};

/** Quản lý tin của tôi: sửa, đổi trạng thái, xóa (FR-07, FR-16). */
export default function MyJobsPage() {
  const [jobs, setJobs] = useState(null);
  const [msg, setMsg] = useState(null);

  const load = () => jobApi.myJobs().then(setJobs).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  useEffect(() => { load(); }, []);

  const run = async (action, okText) => {
    try {
      await action();
      setMsg({ ok: true, text: okText });
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const changeStatus = (job, status, label) => {
    const warn = status === 'FILLED' || status === 'CLOSED' ? '\nCác đơn còn Chờ duyệt sẽ tự chuyển sang Bị từ chối.' : '';
    if (window.confirm(`${label} cho tin “${job.title}”?${warn}`)) {
      run(() => jobApi.changeStatus(job.id, status), 'Đã cập nhật trạng thái');
    }
  };

  const remove = (job) => {
    if (window.confirm(`Xóa tin “${job.title}”? Thao tác này không hoàn tác được.`)) {
      run(() => jobApi.remove(job.id), 'Đã xóa tin');
    }
  };

  return (
    <>
      <PageHeader title="Tin tuyển dụng của tôi" subtitle="Quản lý tin, duyệt ứng viên và cập nhật trạng thái công việc">
        <Link className="btn btn-primary" to="/employer/jobs/new"><i className="bi bi-plus-lg" /> Đăng tin mới</Link>
      </PageHeader>
      {jobs && jobs.length > 0 && (
        <div className="row g-3 mb-4">
          <div className="col-6 col-md-3"><StatCard icon="bi-briefcase" label="Tổng số tin" value={jobs.length} /></div>
          <div className="col-6 col-md-3"><StatCard icon="bi-broadcast" label="Đang tuyển" value={jobs.filter((j) => j.status === 'OPEN').length} color="#15803d" /></div>
          <div className="col-6 col-md-3"><StatCard icon="bi-hourglass-split" label="Ứng viên chờ duyệt" value={jobs.reduce((s, j) => s + j.pendingCount, 0)} color="#b45309" /></div>
          <div className="col-6 col-md-3"><StatCard icon="bi-person-check" label="Đã nhận" value={jobs.reduce((s, j) => s + j.acceptedCount, 0)} color="#0e7490" /></div>
        </div>
      )}
      {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
      {jobs && jobs.length === 0 && <EmptyState icon="bi-megaphone" title="Bạn chưa đăng tin nào"><Link to="/employer/jobs/new">Đăng tin đầu tiên</Link></EmptyState>}
      {jobs?.map((j) => (
        <div key={j.id} className="sv-card sv-card-hover mb-3">
          <div className="card-body p-3 p-md-4">
            <div className="d-flex justify-content-between flex-wrap gap-3">
              <div className="d-flex gap-3">
                <span className="cat-icon">
                  <i className={`bi ${categoryStyle(j.categoryName).icon}`} />
                </span>
                <div>
                <h6 className="fw-semibold mb-1"><Link to={`/jobs/${j.id}`} className="text-body">{j.title}</Link> <JobStatusBadge status={j.status} /></h6>
                <div className="small text-muted d-flex flex-wrap gap-3">
                  <span><i className="bi bi-calendar3" /> Đăng {formatDate(j.createdAt)}</span>
                  <span><i className="bi bi-hourglass" /> Hạn {formatDate(j.deadline)}</span>
                  <span><i className="bi bi-people" /> Đã nhận {j.acceptedCount}/{j.quantity}</span>
                </div>
                <div className="progress mt-2" style={{ height: 6, maxWidth: 260 }}>
                  <div className="progress-bar" style={{ width: `${Math.min(100, (j.acceptedCount / j.quantity) * 100)}%`, background: 'var(--sv-primary)' }} />
                </div>
                {j.status === 'HIDDEN' && <div className="small text-danger mt-1"><i className="bi bi-eye-slash" /> Bị Admin ẩn. Lý do: {j.hiddenReason}</div>}
                </div>
              </div>
              <div className="d-flex flex-wrap gap-2 align-items-start">
                <Link className="btn btn-sm btn-outline-primary" to={`/employer/jobs/${j.id}/applicants`}>
                  <i className="bi bi-people" /> Ứng viên {j.pendingCount > 0 && <span className="badge text-bg-warning">{j.pendingCount} chờ</span>}
                </Link>
                {(j.status === 'OPEN' || j.status === 'FILLED') && (
                  <Link className="btn btn-sm btn-outline-secondary" to={`/employer/jobs/${j.id}/edit`}><i className="bi bi-pencil" /> Sửa</Link>
                )}
                {(NEXT[j.status] || []).map(([status, label, color]) => (
                  <button key={status} className={`btn btn-sm btn-${color}`} onClick={() => changeStatus(j, status, label)}>{label}</button>
                ))}
                {j.acceptedCount === 0 && <button className="btn btn-sm btn-outline-danger" onClick={() => remove(j)}><i className="bi bi-trash" /> Xóa</button>}
              </div>
            </div>
          </div>
        </div>
      ))}
    </>
  );
}
