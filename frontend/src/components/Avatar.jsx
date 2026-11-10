import { colorOf, initials } from '../utils/ui';

/**
 * Logo tạm cho công ty (ô vuông viền mờ, chữ viết tắt có màu)
 * hoặc ảnh đại diện người dùng (round: hình tròn nền màu).
 */
export default function Avatar({ name, size = 48, round = false }) {
  const color = colorOf(name);
  if (round) {
    return (
      <span className="avatar" style={{ width: size, height: size, fontSize: size * 0.38, background: color }}>
        {initials(name)}
      </span>
    );
  }
  return (
    <span className="logo-box" style={{ width: size, height: size, fontSize: size * 0.32, color }}>
      {initials(name)}
    </span>
  );
}
