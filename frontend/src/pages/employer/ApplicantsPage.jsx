import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import applicationApi from '../../api/applicationApi';
import jobApi from '../../api/jobApi';
import { errorMessage } from '../../api/axiosClient';
import { AppStatusBadge, JobStatusBadge } from '../../components/StatusBadge';
import ReviewModal from '../../components/ReviewModal';
import { formatDateTime, openBlob } from '../../utils/format';
import Avatar from '../../components/Avatar';
import EmptyState from '../../components/EmptyState';
import { timeAgo } from '../../utils/ui';

const TABS = [['ALL', 'Tất cả'], ['PENDING', 'Chờ duyệt'], ['ACCEPTED', 'Được nhận'], ['REJECTED', 'Bị từ chối'], ['WITHDRAWN', 'Đã rút']];

/** Danh sách ứng viên của tin (FR-14), duyệt / từ chối (FR-15), đánh giá SV sau khi hoàn thành (FR-18). */
export default function ApplicantsPage() {
  const { id } = useParams();
  const [job, setJob] = useState(null);
  const [list, setList] = useState(null);
  const [msg, setMsg] = useState(null);
  const [reviewing, setReviewing] = useState(null);
  const [tab, setTab] = useState('ALL');

  const load = () => {
    jobApi.detail(id).then(setJob).catch(() => {});
    applicationApi.applicants(id).then(setList).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  };
  useEffect(load, [id]);

  const decide = async (a, status) => {
    const label = status === 'ACCEPTED' ? 'Nhận' : 'Từ chối';
    if (!window.confirm(`${label} ứng viên ${a.fullName}?`)) return;
    try {
      await applicationApi.decide(a.id, status);
      setMsg({ ok: true, text: `Đã ${label.toLowerCase()} ${a.fullName}` });
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  const viewCv = async (a) => {
    try {
      openBlob(await applicationApi.cv(a.id));
    } catch {
      setMsg({ ok: false, text: 'Không mở được CV' });
    }
  };

  return (
    <>
      <Link to="/employer/jobs" className="small"><i className="bi bi-arrow-left" /> Tin của tôi</Link>
      {job && (
        <div className="sv-card p-4 mt-2 mb-3 d-flex flex-wrap justify-content-between gap-3 align-items-center">
          <div>
            <h4 className="fw-bold mb-1">{job.title} <JobStatusBadge status={job.status} /></h4>
            <div className="text-muted small"><i className="bi bi-geo-alt" /> {job.location} · <i className="bi bi-clock" /> {job.workTime}</div>
          </div>
          <div className="text-center">
            <div className="fs-4 fw-bold text-primary">{job.acceptedCount}/{job.quantity}</div>
            <div className="small text-muted">đã nhận</div>
          </div>
        </div>
      )}
      {list && list.length > 0 && (
        <ul className="nav nav-pills gap-1 mb-3 small">
          {TABS.map(([key, label]) => {
            const n = key === 'ALL' ? list.length : list.filter((a) => a.status === key).length;
            return (
              <li className="nav-item" key={key}>
                <button className={`nav-link py-1 ${tab === key ? 'active' : 'bg-white border'}`} onClick={() => setTab(key)}>
                  {label} <span className="opacity-75">({n})</span>
                </button>
              </li>
            );
          })}
        </ul>
      )}
      {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
      {list && list.length === 0 && <EmptyState icon="bi-person-plus" title="Chưa có ứng viên nào">Tin sẽ hiển thị ứng viên ngay khi có sinh viên nộp đơn.</EmptyState>}
      {list?.filter((a) => tab === 'ALL' || a.status === tab).map((a) => (
        <div key={a.id} className="sv-card mb-3">
          <div className="card-body p-3 p-md-4">
            <div className="d-flex justify-content-between flex-wrap gap-2">
              <div className="d-flex gap-3">
                <Avatar name={a.fullName} size={52} round />
                <div>
                <h6 className="fw-semibold mb-1">{a.fullName} <AppStatusBadge status={a.status} /></h6>
                {(a.school || a.major) && (
                  <div className="small text-muted">
                    <i className="bi bi-mortarboard" /> {[a.school, a.major, a.yearOfStudy && `Năm ${a.yearOfStudy}`].filter(Boolean).join(' · ')}
                  </div>
                )}
                <div className="small"><i className="bi bi-envelope text-muted" /> {a.email} {a.phone && <><i className="bi bi-telephone text-muted ms-2" /> {a.phone}</>}</div>
                {a.avgRating != null && <div className="small"><i className="bi bi-star-fill star" /> {a.avgRating.toFixed(1)}/5 · <Link to={`/reviews/${a.studentId}`}>xem đánh giá</Link></div>}
                </div>
              </div>
              <div className="d-flex gap-2 align-items-start flex-wrap">
                {a.hasCv && <button className="btn btn-sm btn-outline-secondary" onClick={() => viewCv(a)}><i className="bi bi-file-earmark-pdf" /> Xem CV</button>}
                {a.status === 'PENDING' && job?.status === 'OPEN' && (
                  <>
                    <button className="btn btn-sm btn-success" onClick={() => decide(a, 'ACCEPTED')}><i className="bi bi-check-lg" /> Nhận</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => decide(a, 'REJECTED')}><i className="bi bi-x-lg" /> Từ chối</button>
                  </>
                )}
                {a.status === 'ACCEPTED' && job?.status === 'COMPLETED' && !a.reviewed && (
                  <button className="btn btn-sm btn-warning" onClick={() => setReviewing(a)}>Đánh giá</button>
                )}
                {a.reviewed && <span className="small text-success">Đã đánh giá</span>}
              </div>
            </div>
            {a.skills && (
              <div className="d-flex flex-wrap gap-1 mt-3">
                {a.skills.split(',').map((s) => s.trim()).filter(Boolean).map((s) => <span key={s} className="tag">{s}</span>)}
              </div>
            )}
            {a.coverLetter && <div className="small mt-2 pre-line p-3 rounded-3 bg-light"><i className="bi bi-quote text-primary" /> {a.coverLetter}</div>}
            <div className="small text-muted mt-2">Nộp {timeAgo(a.appliedAt)} ({formatDateTime(a.appliedAt)})
              {a.processedAt && ` · Xử lý lúc ${formatDateTime(a.processedAt)}`}</div>
          </div>
        </div>
      ))}
      {reviewing && (
        <ReviewModal jobId={Number(id)} reviewee={{ id: reviewing.studentId, name: reviewing.fullName }}
          onClose={() => setReviewing(null)}
          onDone={() => { setReviewing(null); setMsg({ ok: true, text: 'Đã gửi đánh giá' }); load(); }} />
      )}
    </>
  );
}
