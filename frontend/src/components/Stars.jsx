/** Hiển thị điểm đánh giá trung bình (FR-19). */
export default function Stars({ value, count }) {
  if (!count) return <span className="text-muted small"><i className="bi bi-star" /> Chưa có đánh giá</span>;
  return (
    <span className="small">
      <i className="bi bi-star-fill star" /> <b>{Number(value).toFixed(1)}</b>/5 <span className="text-muted">({count} đánh giá)</span>
    </span>
  );
}
