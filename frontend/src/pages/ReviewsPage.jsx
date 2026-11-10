import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import reviewApi from '../api/reviewApi';
import { errorMessage } from '../api/axiosClient';
import { formatDate } from '../utils/format';
import Avatar from '../components/Avatar';

/** Danh sách đánh giá và điểm trung bình của một người dùng (FR-19). */
export default function ReviewsPage() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    reviewApi.ofUser(id).then(setData).catch((err) => setError(errorMessage(err)));
  }, [id]);

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!data) return <div className="text-center text-muted py-5"><div className="spinner-border text-primary" /></div>;
  return (
    <>
      <div className="sv-card p-4 mb-4 d-flex flex-wrap align-items-center gap-4">
        <Avatar name={data.fullName} size={64} />
        <div className="flex-grow-1">
          <div className="text-muted small">Đánh giá về</div>
          <h4 className="fw-bold mb-0">{data.fullName}</h4>
        </div>
        <div className="text-center">
          <div className="display-6 fw-bold">{data.reviewCount ? Number(data.avgRating).toFixed(1) : '–'}</div>
          <div className="star">{[1, 2, 3, 4, 5].map((i) => <i key={i} className={`bi ${i <= Math.round(data.avgRating || 0) ? 'bi-star-fill' : 'bi-star'}`} />)}</div>
          <div className="small text-muted">{data.reviewCount} đánh giá</div>
        </div>
      </div>
      {data.items.map((r) => (
        <div key={r.id} className="sv-card p-3 p-md-4 mb-3 d-flex gap-3">
          <Avatar name={r.reviewerName} size={44} round />
          <div className="flex-grow-1">
            <div className="d-flex justify-content-between flex-wrap">
              <span className="fw-semibold">{r.reviewerName}</span>
              <span className="small text-muted">{formatDate(r.createdAt)}</span>
            </div>
            <div className="star small">{[1, 2, 3, 4, 5].map((i) => <i key={i} className={`bi ${i <= r.rating ? 'bi-star-fill' : 'bi-star'}`} />)}
              <span className="text-muted ms-2">· {r.jobTitle}</span></div>
            {r.comment && <div className="mt-1">{r.comment}</div>}
          </div>
        </div>
      ))}
    </>
  );
}
