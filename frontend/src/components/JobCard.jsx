import { Link } from 'react-router-dom';
import Avatar from './Avatar';
import { formatSalary } from '../utils/format';
import { shiftLabel, hourRange } from '../shifts';
import { daysLeft, isNew, timeAgo } from '../utils/ui';

/** Một dòng tin tuyển dụng trong danh sách: logo, tên việc, công ty, lương, các thẻ thông tin. */
export default function JobCard({ job }) {
  const left = daysLeft(job.deadline);
  return (
    <Link to={`/jobs/${job.id}`} className="d-block sv-card sv-card-hover job-card mb-2 text-reset">
      <div className="d-flex gap-3">
        <Avatar name={job.companyName} size={72} />
        <div className="flex-grow-1" style={{ minWidth: 0 }}>
          <div className="d-flex justify-content-between align-items-start gap-3">
            <div style={{ minWidth: 0 }}>
              <div className="job-title">
                {job.urgent && <span className="tag-urgent me-1"><i className="bi bi-lightning-charge-fill" /> GẤP</span>}
                {isNew(job.createdAt) && <span className="tag-new me-1">MỚI</span>}
                {job.title}
              </div>
              <div className="job-company text-truncate">{job.companyName}</div>
            </div>
            <span className="job-salary d-none d-sm-inline">{formatSalary(job.salaryMin, job.salaryMax, job.salaryUnit)}</span>
          </div>
          <div className="job-salary d-sm-none mt-1">{formatSalary(job.salaryMin, job.salaryMax, job.salaryUnit)}</div>
          <div className="d-flex flex-wrap gap-1 mt-2">
            <span className="tag">{job.location}</span>
            <span className="tag">{job.workTime}</span>
            {job.shift && job.shift !== 'FLEXIBLE' && <span className="tag"><i className="bi bi-clock me-1" />{shiftLabel(job.shift)}</span>}
            {job.startHour != null && <span className="tag">{hourRange(job.startHour, job.endHour)}</span>}
            <span className="tag">{job.categoryName}</span>
            {job.quantity > 1 && <span className="tag">Tuyển {job.quantity} người</span>}
          </div>
          <div className="job-foot d-flex justify-content-between flex-wrap gap-2">
            <span>Cập nhật {timeAgo(job.createdAt)}</span>
            <span className={left <= 3 ? 'tag-urgent' : ''}>
              {left > 0 ? <>Còn <b>{left}</b> ngày để ứng tuyển</> : 'Hết hạn hôm nay'}
            </span>
          </div>
        </div>
      </div>
    </Link>
  );
}
