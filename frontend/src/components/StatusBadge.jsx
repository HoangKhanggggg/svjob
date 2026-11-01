import { APP_STATUS, JOB_STATUS } from '../utils/format';

const SOFT = {
  success: ['#e8f6ee', '#00843f'], primary: ['#e7f0fb', '#1f5fa8'], secondary: ['#f1f5f9', '#475569'],
  dark: ['#e2e8f0', '#1e293b'], danger: ['#fee2e2', '#b91c1c'], warning: ['#fef3c7', '#b45309'],
};

function Soft({ color, children }) {
  const [bg, fg] = SOFT[color] || SOFT.secondary;
  return <span className="badge" style={{ background: bg, color: fg }}>{children}</span>;
}

export function JobStatusBadge({ status }) {
  const s = JOB_STATUS[status] || { text: status, color: 'secondary' };
  return <Soft color={s.color}>{s.text}</Soft>;
}

export function AppStatusBadge({ status }) {
  const s = APP_STATUS[status] || { text: status, color: 'secondary' };
  return <Soft color={s.color}>{s.text}</Soft>;
}
