package vn.vlu.svjob.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;
import vn.vlu.svjob.entity.WorkShift;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Điều kiện lọc tin (FR-09, FR-10). Mỗi tham số khác null sẽ thêm 1 điều kiện AND.
 * Ví dụ: keyword=gia sư, location=Gò Vấp, minSalary=100000, maxSalary=200000.
 */
public final class JobSpecs {

    private JobSpecs() {
    }

    public static Specification<JobPost> filter(JobStatus status, String keyword, Integer categoryId,
                                                String location, Integer minSalary, Integer maxSalary) {
        return filter(status, keyword, categoryId, location, minSalary, maxSalary, null, null);
    }

    /** Thêm bộ lọc ca làm việc và thời gian đăng (trong N ngày gần nhất). */
    public static Specification<JobPost> filter(JobStatus status, String keyword, Integer categoryId,
                                                String location, Integer minSalary, Integer maxSalary,
                                                WorkShift shift, Integer postedWithinDays) {
        return filter(status, keyword, categoryId, location, minSalary, maxSalary, shift, postedWithinDays, null, null);
    }

    /** Thêm lọc khung giờ: tin làm trọn trong khoảng [fromHour, toHour]; tin không cố định giờ bị loại. */
    public static Specification<JobPost> filter(JobStatus status, String keyword, Integer categoryId,
                                                String location, Integer minSalary, Integer maxSalary,
                                                WorkShift shift, Integer postedWithinDays,
                                                Integer fromHour, Integer toHour) {
        return (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (status != null) {
                list.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                list.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (categoryId != null) {
                list.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (location != null && !location.isBlank()) {
                list.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }
            // Khoảng lương của tin phải giao với khoảng lương người dùng chọn
            if (minSalary != null) {
                list.add(cb.greaterThanOrEqualTo(cb.coalesce(root.get("salaryMax"), root.get("salaryMin")), minSalary));
            }
            if (maxSalary != null) {
                list.add(cb.lessThanOrEqualTo(root.get("salaryMin"), maxSalary));
            }
            if (shift != null) {
                // Tin "linh hoạt" phù hợp với mọi ca nên luôn được giữ lại
                list.add(cb.or(cb.equal(root.get("shift"), shift), cb.equal(root.get("shift"), WorkShift.FLEXIBLE)));
            }
            if (postedWithinDays != null && postedWithinDays > 0) {
                list.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        LocalDateTime.now().minusDays(postedWithinDays)));
            }
            if (fromHour != null) {
                list.add(cb.isNotNull(root.get("startHour")));
                list.add(cb.greaterThanOrEqualTo(root.get("startHour"), fromHour));
            }
            if (toHour != null) {
                list.add(cb.isNotNull(root.get("endHour")));
                list.add(cb.lessThanOrEqualTo(root.get("endHour"), toHour));
            }
            return cb.and(list.toArray(new Predicate[0]));
        };
    }
}
