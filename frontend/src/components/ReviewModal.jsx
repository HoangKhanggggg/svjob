import { useState } from 'react';
import reviewApi from '../api/reviewApi';
import { errorMessage } from '../api/axiosClient';

/** Form đánh giá 1–5 sao + nhận xét (FR-18). Hiển thị dạng hộp thoại đơn giản. */
export default function ReviewModal({ jobId, reviewee, onClose, onDone }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await reviewApi.create(jobId, { revieweeId: reviewee.id, rating, comment });
      onDone();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal d-block" style={{ background: 'rgba(15,23,42,.5)' }}>
      <div className="modal-dialog modal-dialog-centered">
        <form className="modal-content" onSubmit={submit}>
          <div className="modal-header">
            <h5 className="modal-title">Đánh giá {reviewee.name}</h5>
            <button type="button" className="btn-close" onClick={onClose} />
          </div>
          <div className="modal-body">
            {error && <div className="alert alert-danger py-2">{error}</div>}
            <div className="mb-3 fs-3">
              {[1, 2, 3, 4, 5].map((n) => (
                <i key={n} className={`bi cursor-pointer me-1 ${n <= rating ? 'bi-star-fill star' : 'bi-star text-secondary'}`} onClick={() => setRating(n)} />
              ))}
              <span className="fs-6 ms-2">{rating}/5</span>
            </div>
            <textarea className="form-control" rows={3} maxLength={500} placeholder="Nhận xét (không bắt buộc)"
              value={comment} onChange={(e) => setComment(e.target.value)} />
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-outline-secondary" onClick={onClose}>Hủy</button>
            <button className="btn btn-primary" disabled={saving}>{saving ? 'Đang gửi...' : 'Gửi đánh giá'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}
