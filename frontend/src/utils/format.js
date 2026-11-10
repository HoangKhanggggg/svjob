export const SALARY_UNITS = { HOUR: 'giờ', SHIFT: 'ca/buổi', MONTH: 'tháng' };

export const JOB_STATUS = {
  OPEN: { text: 'Đang tuyển', color: 'success' },
  FILLED: { text: 'Đã nhận người', color: 'primary' },
  COMPLETED: { text: 'Hoàn thành', color: 'secondary' },
  CLOSED: { text: 'Đã đóng', color: 'dark' },
  HIDDEN: { text: 'Bị ẩn', color: 'danger' },
};

export const APP_STATUS = {
  PENDING: { text: 'Chờ duyệt', color: 'warning' },
  ACCEPTED: { text: 'Được nhận', color: 'success' },
  REJECTED: { text: 'Bị từ chối', color: 'danger' },
  WITHDRAWN: { text: 'Đã rút', color: 'secondary' },
};

const money = (n) => Number(n).toLocaleString('vi-VN');

export function formatSalary(min, max, unit) {
  const range = max && max !== min ? `${money(min)} – ${money(max)}` : money(min);
  return `${range} đ/${SALARY_UNITS[unit] || ''}`;
}

export function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleDateString('vi-VN');
}

export function formatDateTime(value) {
  if (!value) return '';
  return new Date(value).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
}

/** Mở file PDF (blob) trong tab mới. */
export function openBlob(blob) {
  const url = URL.createObjectURL(blob);
  window.open(url, '_blank');
  setTimeout(() => URL.revokeObjectURL(url), 60000);
}
