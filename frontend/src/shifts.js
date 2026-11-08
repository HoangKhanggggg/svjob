// Ca làm việc + mốc thời gian đăng dùng cho bộ lọc và form đăng tin
export const SHIFTS = [
  ['MORNING', 'Ca sáng', 'bi-sunrise'],
  ['NOON', 'Ca trưa', 'bi-brightness-high'],
  ['AFTERNOON', 'Ca chiều', 'bi-sun'],
  ['EVENING', 'Ca tối', 'bi-moon-stars'],
  ['FLEXIBLE', 'Linh hoạt', 'bi-shuffle'],
];
export const shiftLabel = (v) => SHIFTS.find((s) => s[0] === v)?.[1] ?? '';
export const POSTED_WITHIN = [['', 'Mọi lúc'], ['1', '24 giờ qua'], ['7', '7 ngày qua'], ['30', '30 ngày qua']];
export const HOURS = Array.from({ length: 25 }, (_, h) => h);
export const hourRange = (a, b) => (a == null || b == null ? '' : `${a}h – ${b}h`);
