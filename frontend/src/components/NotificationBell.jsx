import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import notificationApi from '../api/notificationApi';
import { timeAgo } from '../utils/ui';

/** Chuông thông báo (FR-17): số chưa đọc, bấm vào để đánh dấu đã đọc. Tự cập nhật mỗi 30 giây. */
export default function NotificationBell() {
  const [data, setData] = useState({ unreadCount: 0, items: [] });
  const navigate = useNavigate();

  const load = () => notificationApi.list().then(setData).catch(() => {});

  useEffect(() => {
    load();
    const timer = setInterval(load, 30000);
    return () => clearInterval(timer);
  }, []);

  const open = async (n) => {
    if (!n.read) await notificationApi.read(n.id);
    load();
    if (n.link) navigate(n.link);
  };

  const readAll = async () => {
    await notificationApi.readAll();
    load();
  };

  return (
    <div className="dropdown">
      <button className="btn btn-light border rounded-circle position-relative" style={{ width: 40, height: 40 }}
        data-bs-toggle="dropdown" aria-label="Thông báo">
        <i className="bi bi-bell" />
        {data.unreadCount > 0 && (
          <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">{data.unreadCount}</span>
        )}
      </button>
      <div className="dropdown-menu dropdown-menu-end noti-menu p-0">
        <div className="d-flex justify-content-between align-items-center px-3 py-2 border-bottom sticky-top bg-white">
          <strong>Thông báo</strong>
          {data.unreadCount > 0 && <button className="btn btn-sm btn-link" onClick={readAll}>Đánh dấu đã đọc hết</button>}
        </div>
        {data.items.length === 0 && <div className="px-3 py-4 text-muted small text-center"><i className="bi bi-bell-slash d-block fs-3 mb-1" />Chưa có thông báo</div>}
        {data.items.map((n) => (
          <button key={n.id} className={`dropdown-item text-wrap small py-2 d-flex gap-2 ${n.read ? '' : 'noti-unread'}`} onClick={() => open(n)}>
            {!n.read ? <span className="noti-dot" /> : <span style={{ width: 8 }} />}
            <span>
              <span className="d-block">{n.content}</span>
              <span className="text-muted" style={{ fontSize: 12 }}>{timeAgo(n.createdAt)}</span>
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
