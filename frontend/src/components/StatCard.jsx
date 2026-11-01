/** Ô số liệu nhỏ ở đầu các trang quản lý. */
export default function StatCard({ icon, label, value, color }) {
  return (
    <div className="sv-card stat-card h-100">
      <div className="label"><span>{label}</span><i className={`bi ${icon}`} /></div>
      <b style={{ color }}>{value}</b>
    </div>
  );
}
