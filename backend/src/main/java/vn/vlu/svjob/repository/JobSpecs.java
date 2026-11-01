package vn.vlu.svjob.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.vlu.svjob.entity.JobPost;
import vn.vlu.svjob.entity.JobStatus;

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
            return cb.and(list.toArray(new Predicate[0]));
        };
    }
}
