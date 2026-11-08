import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import jobApi from '../api/jobApi';
import applicationApi from '../api/applicationApi';
import profileApi from '../api/profileApi';
import { errorMessage } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import { JobStatusBadge } from '../components/StatusBadge';
import Stars from '../components/Stars';
import Avatar from '../components/Avatar';
import { formatDate, formatSalary } from '../utils/format';
import { daysLeft, timeAgo } from '../utils/ui';
import { shiftLabel } from '../shifts';

/** Chi tiết tin + thông tin NTD (FR-11) và form ứng tuyển (FR-12). */
export default function JobDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const [job, setJob] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    jobApi.detail(id).then(setJob).catch((err) => setError(errorMessage(err)));
  }, [id]);

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!job) return <div className="text-center text-muted py-5"><div className="spinner-border text-primary" /></div>;

  const e = job.employer;
  const left = daysLeft(job.deadline);
  const salary = formatSalary(job.salaryMin, job.salaryMax, job.salaryUnit);
  return (
    <>
      <nav aria-label="breadcrumb">
        <ol className="breadcrumb mb-3">
          <li className="breadcrumb-item"><Link to="/">Việc làm</Link></li>
          <li className="breadcrumb-item"><Link to="/">{job.categoryName}</Link></li>
          <li className="breadcrumb-item active text-truncate" aria-current="page">{job.title}</li>
        </ol>
      </nav>
      <div className="row g-4">
        <div className="col-lg-8">
          <div className="sv-card mb-3">
            <div className="detail-head">
              <div className="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-3">
                <h3 className="fw-bold mb-0">{job.urgent && <span className="tag-urgent me-2"><i className="bi bi-lightning-charge-fill" /> GẤP</span>}{job.title}</h3>
                <JobStatusBadge status={job.status} />
              </div>
              <div className="row g-3">
                {[['bi-cash-coin', 'Mức lương', salary], ['bi-geo-alt', 'Địa điểm', job.location], ['bi-hourglass-split', 'Hạn nộp hồ sơ', left > 0 ? `Còn ${left} ngày` : 'Hôm nay']].map(([icon, label, value]) => (
                  <div className="col-sm-4" key={label}>
                    <div className="key-info"><span className="ic"><i className={`bi ${icon}`} /></span><div><small>{label}</small><span>{value}</span></div></div>
                  </div>
                ))}
              </div>
              <div className="small text-muted mt-3"><i className="bi bi-clock-history me-1" />Đăng {timeAgo(job.createdAt)} · Hạn nộp {formatDate(job.deadline)}</div>
            </div>
            <div className="px-4 pb-4">
              <h5 className="content-h">Mô tả công việc</h5>
              <p className="pre-line mb-0" style={{ lineHeight: 1.75 }}>{job.description}</p>
              <h5 className="content-h">Thời gian làm việc</h5>
              <p className="mb-0">{job.workTime}{job.shift && job.shift !== 'FLEXIBLE' && <span className="tag ms-2">{shiftLabel(job.shift)}</span>}</p>
            </div>
          </div>
        </div>
        <div className="col-lg-4">
          <div className="sticky-side">
            <ApplyBox job={job} user={user} />
            <div className="sv-card mt-3">
              <div className="p-3 d-flex gap-3 align-items-center border-bottom">
                <Avatar name={e.companyName} size={56} />
                <div style={{ minWidth: 0 }}>
                  <Link to={`/employers/${e.userId}`} className="fw-semibold text-body d-block">{e.companyName}</Link>
                  <Stars value={e.avgRating} count={e.reviewCount} />
                </div>
              </div>
              <div className="p-3 small">
                {e.address && <div className="mb-1"><i className="bi bi-geo-alt text-muted me-2" />{e.address}</div>}
                {e.contactPhone && <div className="mb-1"><i className="bi bi-telephone text-muted me-2" />{e.contactPhone}</div>}
                <Link to={`/employers/${e.userId}`} className="d-inline-block mt-1">Xem trang công ty <i className="bi bi-box-arrow-up-right" /></Link>
              </div>
            </div>
            <div className="sv-card mt-3">
              <div className="box-title">Thông tin chung</div>
              <ul className="info-list px-3 py-1">
                <li><span>Ngành nghề</span><span>{job.categoryName}</span></li>
                <li><span>Số lượng tuyển</span><span>{job.quantity} người</span></li>
                <li><span>Đã nhận</span><span>{job.acceptedCount} người</span></li>
                <li><span>Hình thức</span><span>Bán thời gian</span></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

function ApplyBox({ job, user }) {
  const [profile, setProfile] = useState(null);
  const [coverLetter, setCoverLetter] = useState('');
  const [cv, setCv] = useState(null);
  const [useProfileCv, setUseProfileCv] = useState(true);
  const [msg, setMsg] = useState(null);
  const [sending, setSending] = useState(false);

  useEffect(() => {
    if (user?.role === 'STUDENT') profileApi.student().then(setProfile).catch(() => {});
  }, [user]);

  if (!user) {
    return (
      <div className="sv-card p-3">
        <p className="small text-muted mb-2">Đăng nhập bằng tài khoản sinh viên để ứng tuyển công việc này.</p>
        <Link to="/login" state={{ from: `/jobs/${job.id}` }} className="btn btn-primary w-100">Đăng nhập để ứng tuyển</Link>
      </div>
    );
  }
  if (user.role !== 'STUDENT') return null;  // Nút ứng tuyển chỉ hiện với SV (FR-11)
  if (job.status !== 'OPEN') return <div className="sv-card p-4 text-center text-muted"><i className="bi bi-slash-circle fs-2" /><div className="mt-2">Tin đã ngừng nhận hồ sơ.</div></div>;
  if (msg?.ok) {
    return (
      <div className="sv-card p-4 text-center">
        <i className="bi bi-check-circle-fill fs-1 text-success" />
        <h6 className="mt-2">Ứng tuyển thành công!</h6>
        <p className="small text-muted mb-0">Theo dõi kết quả ở <Link to="/student/applications">Đơn của tôi</Link>.</p>
      </div>
    );
  }

  const pickFile = (e) => {
    const f = e.target.files[0];
    if (f && !f.name.toLowerCase().endsWith('.pdf')) {
      setMsg({ ok: false, text: 'Chỉ chấp nhận file PDF' });
      e.target.value = '';
      return;
    }
    if (f && f.size > 5 * 1024 * 1024) {
      setMsg({ ok: false, text: 'File tối đa 5 MB' });
      e.target.value = '';
      return;
    }
    setMsg(null);
    setCv(f || null);
  };

  const hasProfileCv = profile?.hasCv;
  const submit = async (e) => {
    e.preventDefault();
    if (!cv && !(useProfileCv && hasProfileCv) && !coverLetter.trim()) {
      setMsg({ ok: false, text: 'Cần đính kèm CV hoặc viết lời giới thiệu' });
      return;
    }
    setSending(true);
    try {
      await applicationApi.apply(job.id, { cv, coverLetter, useProfileCv: useProfileCv && hasProfileCv });
      setMsg({ ok: true });
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="sv-card">
      <div className="box-title">Ứng tuyển công việc này</div>
      <form className="p-3" onSubmit={submit}>
        {msg && !msg.ok && <div className="alert alert-danger py-2">{msg.text}</div>}
        {hasProfileCv && (
          <div className="form-check mb-2">
            <input id="useCv" type="checkbox" className="form-check-input" checked={useProfileCv && !cv}
              disabled={!!cv} onChange={(e) => setUseProfileCv(e.target.checked)} />
            <label htmlFor="useCv" className="form-check-label">Dùng CV trong hồ sơ của tôi</label>
          </div>
        )}
        <div className="mb-2">
          <label className="form-label small">Hoặc tải CV khác (PDF ≤ 5 MB)</label>
          <input type="file" accept="application/pdf" className="form-control form-control-sm" onChange={pickFile} />
        </div>
        <div className="mb-3">
          <label className="form-label small">Lời giới thiệu</label>
          <textarea className="form-control" rows={4} maxLength={1000} value={coverLetter}
            placeholder="Giới thiệu ngắn về bạn và lịch rảnh" onChange={(e) => setCoverLetter(e.target.value)} />
        </div>
        <button className="btn btn-primary w-100 py-2" disabled={sending}>{sending ? 'Đang gửi...' : 'Nộp đơn'}</button>
      </form>
    </div>
  );
}
