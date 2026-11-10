// Các hàm hỗ trợ giao diện: màu avatar, icon ngành nghề, số ngày còn lại...

const COLORS = ['#1f6f8b', '#00875a', '#b35c00', '#5b6b7a', '#c0392b', '#2d6a4f', '#3b5bdb', '#8a5a44', '#0b7285', '#6c5b7b'];

export function colorOf(text = '') {
  let h = 0;
  for (const ch of text) h = (h * 31 + ch.charCodeAt(0)) % 9973;
  return COLORS[h % COLORS.length];
}

export function initials(name = '') {
  const words = name.replace(/[–-]/g, ' ').split(' ').filter(Boolean);
  if (words.length === 0) return '?';
  const pick = words.length >= 2 ? [words[words.length - 2], words[words.length - 1]] : [words[0]];
  return pick.map((w) => w[0]).join('').toUpperCase();
}

const CATEGORY_ICONS = [
  ['Phục vụ', 'bi-cup-hot'],
  ['Bán hàng', 'bi-bag'],
  ['Gia sư', 'bi-mortarboard'],
  ['Nhập liệu', 'bi-keyboard'],
  ['Sự kiện', 'bi-calendar2-event'],
  ['Giao hàng', 'bi-truck'],
  ['Thiết kế', 'bi-palette'],
  ['Lễ tân', 'bi-headset'],
  ['Kho vận', 'bi-box-seam'],
  ['IT', 'bi-laptop'],
];

export function categoryStyle(name = '') {
  const found = CATEGORY_ICONS.find(([key]) => name.startsWith(key));
  return { icon: found ? found[1] : 'bi-briefcase' };
}

export function daysLeft(deadline) {
  const ms = new Date(deadline + 'T23:59:59') - new Date();
  return Math.ceil(ms / 86400000);
}

export function isNew(createdAt) {
  return (new Date() - new Date(createdAt)) / 86400000 <= 1;
}

export function timeAgo(value) {
  const minutes = Math.floor((new Date() - new Date(value)) / 60000);
  if (minutes < 1) return 'vừa xong';
  if (minutes < 60) return `${minutes} phút trước`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} giờ trước`;
  const days = Math.floor(hours / 24);
  return days < 30 ? `${days} ngày trước` : new Date(value).toLocaleDateString('vi-VN');
}
