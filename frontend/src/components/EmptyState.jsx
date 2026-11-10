export default function EmptyState({ icon = 'bi-inbox', title, children }) {
  return (
    <div className="sv-card empty-state">
      <i className={`bi ${icon}`} />
      <h6 className="mt-3 text-body">{title}</h6>
      {children && <div className="small">{children}</div>}
    </div>
  );
}
