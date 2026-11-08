import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import applicationApi from '../../api/applicationApi';
import { errorMessage } from '../../api/axiosClient';
import { AppStatusBadge, JobStatusBadge } from '../../components/StatusBadge';
import ReviewModal from '../../components/ReviewModal';
import { formatDate } from '../../utils/format';
import PageHeader from '../../components/PageHeader';
import StatCard from '../../components/StatCard';
import EmptyState from '../../components/EmptyState';
import Avatar from '../../components/Avatar';

/** Đơn của tôi + rút đơn (FR-13) + đánh giá NTD khi công việc Hoàn thành (FR-18). */
export default function MyApplicationsPage() {
  const [list, setList] = useState(null);
  const [msg, setMsg] = useState(null);
  const [reviewing, setReviewing] = useState(null);

  const load = () => applicationApi.mine().then(setList).catch((err) => setMsg({ ok: false, text: errorMessage(err) }));
  useEffect(() => { load(); }, []);

  const withdraw = async (a) => {
    if (!window.confirm(`Rút đơn ứng tuyển “${a.jobTitle}”?`)) return;
    try {
      await applicationApi.withdraw(a.id);
      setMsg({ ok: true, text: 'Đã rút đơn' });
      load();
    } catch (err) {
      setMsg({ ok: false, text: errorMessage(err) });
    }
  };

  return (
    <>
      <PageHeader title="Đơn ứng tuyển của tôi" subtitle="Theo dõi trạng thái các đơn đã nộp">
        <Link className="btn btn-primary" to="/"><i className="bi bi-search" /> Tìm thêm việc</Link>
      </PageHeader>
      {list && list.length > 0 && (
        <div className="row g-3 mb-4">
          <div className="col-6 col-md-3"><StatCard icon="bi-send" label="Đã nộp" value={list.length} /></div>
          <div className="col-6 col-md-3"><StatCard icon="bi-hourglass-split" label="Chờ duyệt" value={list.filter((a) => a.status === 'PENDING').length} color="#b45309" /></div>
          <div className="col-6 col-md-3"><StatCard icon="bi-check-circle" label="Được nhận" value={list.filter((a) => a.status === 'ACCEPTED').length} color="#15803d" /></div>
          <div className="col-6 col-md-3"><StatCard icon="bi-x-circle" label="Bị từ chối" value={list.filter((a) => a.status === 'REJECTED').length} color="#b91c1c" /></div>
        </div>
      )}
      {msg && <div className={`alert py-2 alert-${msg.ok ? 'success' : 'danger'}`}>{msg.text}</div>}
      {list && list.length === 0 && <EmptyState icon="bi-send" title="Bạn chưa ứng tuyển công việc nào"><Link to="/">Tìm việc ngay</Link></EmptyState>}
      {list && list.length > 0 && (
        <div className="sv-card table-responsive overflow-hidden">
          <table className="table sv-table align-middle mb-0">
            <thead><tr><th>Công việc</th><th>Ngày nộp</th><th>Trạng thái đơn</th><th>Tin</th><th /></tr></thead>
            <tbody>
              {list.map((a) => (
                <tr key={a.id}>
                  <td>
                    <div className="d-flex gap-2 align-items-center">
                      <Avatar name={a.companyName} size={38} />
                      <div><Link to={`/jobs/${a.jobId}`} className="fw-medium text-body">{a.jobTitle}</Link><div className="small text-muted">{a.companyName}</div></div>
                    </div>
                  </td>
                  <td>{formatDate(a.appliedAt)}</td>
                  <td><AppStatusBadge status={a.status} /></td>
                  <td><JobStatusBadge status={a.jobStatus} /></td>
                  <td className="text-end">
                    {a.status === 'PENDING' && <button className="btn btn-sm btn-outline-danger" onClick={() => withdraw(a)}>Rút đơn</button>}
                    {a.canReview && !a.reviewed && (
                      <button className="btn btn-sm btn-warning" onClick={() => setReviewing(a)}><i className="bi bi-star" /> Đánh giá NTD</button>
                    )}
                    {a.reviewed && <span className="small text-success"><i className="bi bi-check2-circle" /> Đã đánh giá</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {reviewing && (
        <ReviewModal jobId={reviewing.jobId} reviewee={{ id: reviewing.employerId, name: reviewing.companyName }}
          onClose={() => setReviewing(null)}
          onDone={() => { setReviewing(null); setMsg({ ok: true, text: 'Cảm ơn bạn đã đánh giá' }); load(); }} />
      )}
    </>
  );
}
