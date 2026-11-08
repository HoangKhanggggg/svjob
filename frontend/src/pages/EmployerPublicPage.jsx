import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import profileApi from '../api/profileApi';
import { errorMessage } from '../api/axiosClient';
import JobCard from '../components/JobCard';
import Stars from '../components/Stars';
import Avatar from '../components/Avatar';
import EmptyState from '../components/EmptyState';

/** Trang công khai của nhà tuyển dụng: thông tin, điểm đánh giá (FR-19), tin đang tuyển. */
export default function EmployerPublicPage() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    profileApi.publicEmployer(id).then(setData).catch((err) => setError(errorMessage(err)));
  }, [id]);

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!data) return <div className="text-center text-muted py-5"><div className="spinner-border text-primary" /></div>;
  const p = data.profile;
  return (
    <>
      <div className="sv-card mb-4 overflow-hidden">
        <div className="cover" />
        <div className="px-4 pb-4" style={{ marginTop: -40 }}>
          <div className="d-flex flex-wrap align-items-end gap-3">
            <span className="bg-white p-1 rounded-2 d-inline-flex border"><Avatar name={p.companyName} size={84} /></span>
            <div className="pb-1">
              <h3 className="fw-bold mb-0">{p.companyName}</h3>
              <Stars value={p.avgRating} count={p.reviewCount} />
              {p.reviewCount > 0 && <Link className="small ms-2" to={`/reviews/${p.userId}`}>Xem đánh giá</Link>}
            </div>
          </div>
          <div className="d-flex flex-wrap gap-2 mt-3">
            {p.address && <span className="tag"><i className="bi bi-geo-alt me-1" />{p.address}</span>}
            {p.contactPhone && <span className="tag"><i className="bi bi-telephone me-1" />{p.contactPhone}</span>}
            <span className="tag"><i className="bi bi-briefcase me-1" />{data.openJobs.length} tin đang tuyển</span>
          </div>
          {p.description && <p className="mt-3 mb-0 pre-line">{p.description}</p>}
        </div>
      </div>
      <h5 className="section-title mb-3">Tin đang tuyển ({data.openJobs.length})</h5>
      {data.openJobs.length === 0 && <EmptyState icon="bi-briefcase" title="Hiện chưa có tin đang tuyển" />}
      {data.openJobs.map((j) => <JobCard key={j.id} job={j} />)}
    </>
  );
}
