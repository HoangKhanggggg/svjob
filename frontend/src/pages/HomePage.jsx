import { useEffect, useState } from 'react';
import jobApi from '../api/jobApi';
import { errorMessage } from '../api/axiosClient';
import JobCard from '../components/JobCard';
import Pagination from '../components/Pagination';
import EmptyState from '../components/EmptyState';

const EMPTY = { keyword: '', categoryId: '', location: '', minSalary: '', maxSalary: '' };
const HOT_KEYS = ['Phục vụ', 'Gia sư', 'Bán hàng', 'Pha chế', 'Nhập liệu', 'Sự kiện'];
const AREAS = ['Gò Vấp', 'Bình Thạnh', 'Quận 1', 'Thủ Đức', 'Quận 3', 'Quận 10', 'Phú Nhuận', 'Tân Bình', 'Online'];
const SALARY_RANGES = [['Tất cả', '', ''], ['Dưới 30.000 đ', '', '30000'], ['30.000 – 100.000 đ', '30000', '100000'],
  ['100.000 – 300.000 đ', '100000', '300000'], ['Trên 300.000 đ', '300000', '']];

/** Trang chủ: danh sách tin Đang tuyển, tìm kiếm + lọc kết hợp, phân trang (FR-09, FR-10). */
export default function HomePage() {
  const [categories, setCategories] = useState([]);
  const [stats, setStats] = useState(null);
  const [filter, setFilter] = useState(EMPTY);
  const [applied, setApplied] = useState(EMPTY);
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    jobApi.categories().then(setCategories).catch(() => {});
    jobApi.stats().then(setStats).catch(() => {});
  }, []);

  useEffect(() => {
    const params = { page, size: 10 };
    Object.entries(applied).forEach(([k, v]) => { if (v !== '') params[k] = v; });
    setError('');
    jobApi.search(params).then(setData).catch((err) => setError(errorMessage(err)));
  }, [applied, page]);

  const apply = (next) => {
    setFilter(next);
    setPage(0);
    setApplied(next);
  };

  const search = (e) => {
    e.preventDefault();
    if (filter.minSalary && filter.maxSalary && Number(filter.minSalary) > Number(filter.maxSalary)) {
      setError('Lương từ phải nhỏ hơn hoặc bằng lương đến');
      return;
    }
    apply(filter);
  };

  const pickCategory = (id) => apply({ ...applied, categoryId: String(applied.categoryId) === String(id) ? '' : String(id) });
  const pickArea = (area) => apply({ ...applied, location: applied.location === area ? '' : area });
  const pickSalary = (min, max) => apply({ ...applied, minSalary: min, maxSalary: max });
  const hotKey = (text) => apply({ ...EMPTY, keyword: text });
  const set = (key) => (e) => setFilter({ ...filter, [key]: e.target.value });
  const filtered = JSON.stringify(applied) !== JSON.stringify(EMPTY);
  const countOf = (id) => stats?.categories.find((c) => c.id === id)?.openJobs ?? 0;
  const salaryActive = (min, max) => applied.minSalary === min && applied.maxSalary === max;
  const currentCategory = categories.find((c) => String(c.id) === String(applied.categoryId));

  return (
    <>
      <section className="search-band">
        <div className="container">
          <h1>Tìm việc làm thêm phù hợp với lịch học</h1>
          <p>Tin tuyển dụng part-time từ quán cà phê, cửa hàng, trung tâm gia sư và sự kiện quanh TP.HCM</p>
          <form className="search-box row g-0" onSubmit={search}>
            <div className="col-md-5 cell">
              <i className="bi bi-search" />
              <input className="form-control" placeholder="Tên công việc, vị trí bạn muốn ứng tuyển..." value={filter.keyword} onChange={set('keyword')} />
            </div>
            <div className="col-md-3 cell">
              <i className="bi bi-briefcase" />
              <select className="form-select" value={filter.categoryId} onChange={set('categoryId')}>
                <option value="">Tất cả ngành nghề</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div className="col-md-2 cell">
              <i className="bi bi-geo-alt" />
              <input className="form-control" placeholder="Quận / Online" value={filter.location} onChange={set('location')} />
            </div>
            <div className="col-md-2 ps-md-2 pt-2 pt-md-0 d-grid">
              <button className="btn btn-primary px-3">Tìm kiếm</button>
            </div>
          </form>
          <div className="hot-keys">
            <span className="me-2">Từ khóa phổ biến:</span>
            {HOT_KEYS.map((k) => <button key={k} type="button" onClick={() => hotKey(k)}>{k}</button>)}
          </div>
          {stats && (
            <div className="quick-stats d-none d-md-flex flex-wrap gap-4">
              <span><b>{stats.openJobs}</b> việc đang tuyển</span>
              <span><b>{stats.employers}</b> nhà tuyển dụng</span>
              <span><b>{stats.students}</b> sinh viên đã tham gia</span>
              <span><b>{stats.completedJobs}</b> công việc đã hoàn thành</span>
            </div>
          )}
        </div>
      </section>

      <div className="container mt-4">
        <div className="row g-4">
          <aside className="col-lg-3 d-none d-lg-block">
            <div className="sv-card">
              <div className="box-title d-flex justify-content-between align-items-center">
                <span><i className="bi bi-funnel me-1" /> Lọc nâng cao</span>
                {filtered && <button className="btn btn-link btn-sm p-0 text-danger" onClick={() => apply(EMPTY)}>Xóa lọc</button>}
              </div>
              <div className="filter-group">
                <h6>Ngành nghề</h6>
                {categories.map((c) => (
                  <button key={c.id} className={`filter-item ${String(applied.categoryId) === String(c.id) ? 'active' : ''}`} onClick={() => pickCategory(c.id)}>
                    <span><span className="radio" />{c.name}</span><span className="count">{countOf(c.id)}</span>
                  </button>
                ))}
              </div>
              <div className="filter-group">
                <h6>Khu vực</h6>
                <div className="d-flex flex-wrap gap-1">
                  {AREAS.map((a) => (
                    <button key={a} className={`btn btn-sm ${applied.location === a ? 'btn-primary' : 'btn-light border'}`} onClick={() => pickArea(a)}>{a}</button>
                  ))}
                </div>
              </div>
              <div className="filter-group">
                <h6>Mức lương</h6>
                {SALARY_RANGES.map(([label, min, max]) => (
                  <button key={label} className={`filter-item ${salaryActive(min, max) ? 'active' : ''}`} onClick={() => pickSalary(min, max)}>
                    <span><span className="radio" />{label}</span>
                  </button>
                ))}
                <form className="d-flex gap-1 mt-2" onSubmit={search}>
                  <input type="number" min="0" className="form-control form-control-sm" placeholder="Từ (đ)" title="Lương từ (đồng)" value={filter.minSalary} onChange={set('minSalary')} />
                  <input type="number" min="0" className="form-control form-control-sm" placeholder="Đến (đ)" title="Lương đến (đồng)" value={filter.maxSalary} onChange={set('maxSalary')} />
                  <button className="btn btn-sm btn-soft px-2" title="Áp dụng"><i className="bi bi-arrow-right" /></button>
                </form>
              </div>
            </div>
            <div className="sv-card p-3 mt-3 d-none d-lg-block">
              <div className="fw-bold mb-1">Bạn đang cần tuyển người?</div>
              <div className="small text-muted mb-2">Đăng tin miễn phí, tin được duyệt trong ngày và hiển thị cho sinh viên quanh khu vực của bạn.</div>
              <a href="/register" className="btn btn-outline-primary btn-sm w-100">Đăng tin tuyển dụng</a>
            </div>
          </aside>

          <div className="col-lg-9">
            <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
              <h2 className="section-title mb-0">
                {filtered ? (currentCategory ? `Việc làm ${currentCategory.name}` : 'Kết quả tìm kiếm') : 'Việc làm mới nhất'}
              </h2>
              {data && <span className="text-muted small">Tìm thấy <b className="text-body">{data.totalElements}</b> việc làm phù hợp</span>}
            </div>
            {error && <div className="alert alert-danger">{error}</div>}
            {!data && !error && <div className="text-center text-muted py-5"><div className="spinner-border text-primary" /></div>}
            {data && (
              <>
                {data.items.length === 0 && (
                  <EmptyState icon="bi-search" title="Không có công việc phù hợp với điều kiện tìm kiếm">Hãy thử bỏ bớt bộ lọc.</EmptyState>
                )}
                {data.items.map((job) => <JobCard key={job.id} job={job} />)}
                <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
