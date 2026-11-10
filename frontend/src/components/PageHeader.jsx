/** Tiêu đề trang + mô tả ngắn, nút hành động ở bên phải. */
export default function PageHeader({ title, subtitle, children }) {
  return (
    <div className="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-3 pb-3 border-bottom">
      <div>
        <h4 className="fw-bold mb-1">{title}</h4>
        {subtitle && <div className="text-muted small">{subtitle}</div>}
      </div>
      {children && <div className="d-flex gap-2">{children}</div>}
    </div>
  );
}
