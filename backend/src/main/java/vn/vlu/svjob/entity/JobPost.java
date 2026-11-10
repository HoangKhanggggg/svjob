package vn.vlu.svjob.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_posts")
@Getter
@Setter
@NoArgsConstructor
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private JobCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "salary_min", nullable = false)
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_unit", nullable = false)
    private SalaryUnit salaryUnit;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "work_time", nullable = false, length = 200)
    private String workTime;

    /** Ca làm việc để lọc nhanh: sáng / chiều / tối / linh hoạt. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkShift shift = WorkShift.FLEXIBLE;

    /** Giờ bắt đầu / kết thúc (0–24) để sinh viên lọc theo khung giờ; null = không cố định giờ. */
    @Column(name = "start_hour")
    private Integer startHour;

    @Column(name = "end_hour")
    private Integer endHour;

    /** Tin "Tuyển gấp": hiện huy hiệu và được ưu tiên đưa lên đầu danh sách. */
    @Column(nullable = false)
    private boolean urgent = false;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.OPEN;

    @Column(name = "hidden_reason")
    private String hiddenReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
