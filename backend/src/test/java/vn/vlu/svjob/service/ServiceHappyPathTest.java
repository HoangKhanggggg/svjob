package vn.vlu.svjob.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import vn.vlu.svjob.dto.CommonDtos.CategoryRequest;
import vn.vlu.svjob.dto.ProfileDtos.EmployerProfileRequest;
import vn.vlu.svjob.dto.ProfileDtos.StudentProfileRequest;
import vn.vlu.svjob.dto.ReviewDtos.ReviewRequest;
import vn.vlu.svjob.entity.*;
import vn.vlu.svjob.repository.*;
import vn.vlu.svjob.security.UserPrincipal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static vn.vlu.svjob.service.TestData.*;

/** Các trường hợp thành công của tầng Service (bổ sung độ phủ cho NFR-04). */
@ExtendWith(MockitoExtension.class)
class ServiceHappyPathTest {

    @Mock UserRepository userRepository;
    @Mock StudentProfileRepository studentProfileRepository;
    @Mock EmployerProfileRepository employerProfileRepository;
    @Mock JobPostRepository jobPostRepository;
    @Mock JobCategoryRepository categoryRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock ReviewRepository reviewRepository;
    @Mock NotificationRepository notificationRepository;

    private final User employer = user(2, Role.EMPLOYER);
    private final User student = user(3, Role.STUDENT);
    private static final byte[] PDF = "%PDF-1.4 test".getBytes();

    private FileStorageService storage() throws Exception {
        return new FileStorageService(Files.createTempDirectory("svjob").toString());
    }

    private ProfileService profileService(FileStorageService fs) {
        return new ProfileService(userRepository, studentProfileRepository, employerProfileRepository,
                jobPostRepository, reviewRepository, fs);
    }

    private NotificationService notifications() {
        return new NotificationService(notificationRepository);
    }

    private JobService jobService(ProfileService ps) {
        return new JobService(jobPostRepository, applicationRepository, userRepository,
                new CategoryService(categoryRepository), ps, notifications());
    }

    private void employerProfileExists() {
        lenient().when(employerProfileRepository.findByUserId(2L))
                .thenReturn(Optional.of(new EmployerProfile(employer, "The Coffee Corner")));
    }

    // ---------- FileStorageService ----------

    @Test
    void luuCvPdfHopLe_roiDocLaiDuoc() throws Exception {
        FileStorageService fs = storage();
        String name = fs.storeCv(new MockMultipartFile("file", "cv.pdf", "application/pdf", PDF));
        assertThat(name).endsWith(".pdf");
        assertThat(fs.loadCv(name).exists()).isTrue();
        assertThatThrownBy(() -> fs.loadCv("../ngoai-thu-muc.pdf")).hasMessage("Không tìm thấy file CV");
    }

    @Test
    void fileDoiDuoiThanhPdf_biTuChoi() throws Exception {
        FileStorageService fs = storage();
        assertThatThrownBy(() -> fs.storeCv(new MockMultipartFile("file", "cv.pdf", "application/pdf", "PK..".getBytes())))
                .hasMessage("Chỉ chấp nhận file PDF");
        assertThatThrownBy(() -> fs.storeCv(new MockMultipartFile("file", "cv.pdf", "application/pdf", new byte[0])))
                .hasMessage("Vui lòng chọn file CV");
    }

    // ---------- ProfileService ----------

    @Test
    void capNhatHoSoSinhVien_vaTaiCv() throws Exception {
        StudentProfile p = new StudentProfile(student);
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));
        when(studentProfileRepository.findByUserId(3L)).thenReturn(Optional.of(p));
        when(reviewRepository.averageRating(3L)).thenReturn(4.5);
        ProfileService ps = profileService(storage());

        var res = ps.updateStudent(3L, new StudentProfileRequest("Nguyễn Văn An", "0901234567", "Đại học Văn Lang",
                "CNPM", 3, "Excel", " "));
        assertThat(res.school()).isEqualTo("Đại học Văn Lang");
        assertThat(res.bio()).isNull();
        assertThat(res.avgRating()).isEqualTo(4.5);

        var withCv = ps.uploadCv(3L, new MockMultipartFile("file", "cv.pdf", "application/pdf", PDF));
        assertThat(withCv.hasCv()).isTrue();
        assertThat(ps.myCv(3L).exists()).isTrue();
    }

    @Test
    void capNhatHoSoNtd_vaXemTrangCongKhai() {
        employerProfileExists();
        when(userRepository.findById(2L)).thenReturn(Optional.of(employer));
        when(jobPostRepository.findByEmployerIdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(job(1, employer, JobStatus.OPEN, 2), job(2, employer, JobStatus.CLOSED, 1)));
        ProfileService ps = profileService(null);

        var res = ps.updateEmployer(2L, new EmployerProfileRequest("Chị Lan", "The Coffee Corner 2", "Bình Thạnh", "", ""));
        assertThat(res.companyName()).isEqualTo("The Coffee Corner 2");
        var pub = ps.publicEmployer(2L);
        assertThat(pub.openJobs()).hasSize(1);
    }

    @Test
    void xemHoSoNtdCuaTaiKhoanSinhVien_bao404() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));
        assertThatThrownBy(() -> profileService(null).getEmployer(3L)).hasMessage("Không tìm thấy nhà tuyển dụng");
    }

    // ---------- CategoryService ----------

    @Test
    void themSuaAnNganhNghe() {
        CategoryService cs = new CategoryService(categoryRepository);
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var created = cs.create(new CategoryRequest(" Giao hàng ", null, null));
        assertThat(created.name()).isEqualTo("Giao hàng");

        JobCategory c = new JobCategory();
        c.setId(9);
        c.setName("Giao hàng");
        when(categoryRepository.findById(9)).thenReturn(Optional.of(c));
        cs.update(9, new CategoryRequest("Giao hàng", "Shipper", true));
        assertThat(c.getDescription()).isEqualTo("Shipper");
        cs.hide(9);
        assertThat(c.isActive()).isFalse();
        assertThatThrownBy(() -> cs.findActive(9)).hasMessage("Ngành nghề này đã bị ẩn");

        when(categoryRepository.existsByNameIgnoreCase("Gia sư")).thenReturn(true);
        assertThatThrownBy(() -> cs.create(new CategoryRequest("Gia sư", null, null))).hasMessage("Ngành nghề đã tồn tại");
        when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of());
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(c));
        assertThat(cs.activeCategories()).isEmpty();
        assertThat(cs.allCategories()).hasSize(1);
    }

    // ---------- NotificationService ----------

    @Test
    void danhDauThongBaoDaDoc() {
        Notification n = new Notification(student, "Đơn đã được nhận", "/student/applications");
        n.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.findTop50ByUserIdOrderByCreatedAtDescIdDesc(3L)).thenReturn(List.of(n));
        when(notificationRepository.countByUserIdAndReadFalse(3L)).thenReturn(1L);
        NotificationService ns = notifications();

        assertThat(ns.myNotifications(3L).unreadCount()).isEqualTo(1);
        assertThatThrownBy(() -> ns.markRead(99L, 1L)).hasMessage("Bạn không có quyền với thông báo này");
        ns.markRead(3L, 1L);
        assertThat(n.isRead()).isTrue();

        Notification n2 = new Notification(student, "x", null);
        when(notificationRepository.findByUserIdAndReadFalse(3L)).thenReturn(List.of(n2));
        ns.markAllRead(3L);
        assertThat(n2.isRead()).isTrue();
    }

    // ---------- JobService ----------

    @Test
    @SuppressWarnings("unchecked")
    void timKiemVaDangTin() {
        employerProfileExists();
        JobService js = jobService(profileService(null));
        when(jobPostRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job(1, employer, JobStatus.OPEN, 2))));
        var page = js.search("phục vụ", 1, "Bình Thạnh", 20000, 30000, 0, 50);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).companyName()).isEqualTo("The Coffee Corner");

        JobCategory cat = new JobCategory();
        cat.setId(1);
        cat.setName("Phục vụ - F&B");
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(userRepository.getReferenceById(2L)).thenReturn(employer);
        when(userRepository.findById(2L)).thenReturn(Optional.of(employer));
        var created = js.create(2L, jobRequest(25000, 30000));
        assertThat(created.status()).isEqualTo(JobStatus.OPEN);
        verify(jobPostRepository).save(any());
    }

    @Test
    void suaTinVaXemTinCuaToi() {
        employerProfileExists();
        JobService js = jobService(profileService(null));
        JobPost job = job(1, employer, JobStatus.OPEN, 2);
        JobCategory cat = job.getCategory();
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        var updated = js.update(2L, 1L, new vn.vlu.svjob.dto.JobDtos.JobRequest("Phục vụ ca tối", 1, "Mới", 26000, null,
                SalaryUnit.HOUR, "Bình Thạnh", "Tối", 3, java.time.LocalDate.now().plusDays(5)));
        assertThat(updated.title()).isEqualTo("Phục vụ ca tối");

        when(jobPostRepository.findByEmployerIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(job));
        when(applicationRepository.countByJobIdAndStatus(1L, ApplicationStatus.PENDING)).thenReturn(4L);
        assertThat(js.myJobs(2L).get(0).pendingCount()).isEqualTo(4);

        job.setStatus(JobStatus.COMPLETED);
        assertThatThrownBy(() -> js.update(2L, 1L, jobRequest(25000, 30000)))
                .hasMessage("Không thể sửa tin ở trạng thái hiện tại");
    }

    @Test
    void xoaTinChuaCoNguoiDuocNhan_thanhCong() {
        JobService js = jobService(profileService(null));
        JobPost job = job(1, employer, JobStatus.OPEN, 2);
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job));
        js.delete(2L, 1L);
        verify(jobPostRepository).delete(job);
    }

    @Test
    void tinBiAn_khachKhongXemDuoc_adminXemDuoc_boAnTroVeDangTuyen() {
        employerProfileExists();
        JobService js = jobService(profileService(null));
        JobPost job = job(1, employer, JobStatus.OPEN, 2);
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job));

        js.hide(1L, true, "Tin lừa đảo");
        assertThat(job.getStatus()).isEqualTo(JobStatus.HIDDEN);
        assertThatThrownBy(() -> js.detail(1L, null)).hasMessage("Tin tuyển dụng không tồn tại hoặc đã bị ẩn");
        assertThat(js.detail(1L, new UserPrincipal(1L, "admin@svjob.vn", Role.ADMIN)).status()).isEqualTo(JobStatus.HIDDEN);

        js.hide(1L, false, null);
        assertThat(job.getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(job.getHiddenReason()).isNull();
        verify(notificationRepository, times(2)).save(any());
    }

    @Test
    void hoanThanhCongViec_thongBaoChoNguoiDuocNhan() {
        employerProfileExists();
        JobService js = jobService(profileService(null));
        JobPost job = job(1, employer, JobStatus.FILLED, 1);
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobIdAndStatus(1L, ApplicationStatus.ACCEPTED))
                .thenReturn(List.of(application(5, job, student, ApplicationStatus.ACCEPTED)));
        js.changeStatus(2L, 1L, JobStatus.COMPLETED);
        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        verify(notificationRepository).save(any());
    }

    // ---------- ApplicationService ----------

    private ApplicationService applicationService(FileStorageService fs) {
        ProfileService ps = profileService(fs);
        return new ApplicationService(applicationRepository, studentProfileRepository, userRepository,
                reviewRepository, jobService(ps), ps, fs, notifications());
    }

    @Test
    void ungTuyenKemCv_donChoDuyetVaNtdNhanThongBao() throws Exception {
        ApplicationService as = applicationService(storage());
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job(1, employer, JobStatus.OPEN, 2)));
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        var res = as.apply(3L, 1L, new MockMultipartFile("cv", "cv.pdf", "application/pdf", PDF), "Em rảnh buổi tối", false);
        assertThat(res.status()).isEqualTo(ApplicationStatus.PENDING);
        verify(applicationRepository).save(any());
        verify(notificationRepository).save(any());
    }

    @Test
    void ungTuyenDungCvTrongHoSo() throws Exception {
        ApplicationService as = applicationService(storage());
        StudentProfile p = new StudentProfile(student);
        p.setCvUrl("abc.pdf");
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job(1, employer, JobStatus.OPEN, 2)));
        when(studentProfileRepository.findByUserId(3L)).thenReturn(Optional.of(p));
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        as.apply(3L, 1L, null, null, true);
        verify(applicationRepository).save(argThat(a -> "abc.pdf".equals(a.getCvUrl())));
    }

    @Test
    void donCuaToi_danhSachUngVien_vaQuyenTaiCv() throws Exception {
        employerProfileExists();
        ApplicationService as = applicationService(storage());
        JobPost job = job(1, employer, JobStatus.COMPLETED, 1);
        Application a = application(5, job, student, ApplicationStatus.ACCEPTED);
        when(applicationRepository.findByStudentIdOrderByAppliedAtDesc(3L)).thenReturn(List.of(a));
        var mine = as.myApplications(3L);
        assertThat(mine.get(0).canReview()).isTrue();
        assertThat(mine.get(0).reviewed()).isFalse();

        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobIdOrderByAppliedAtDesc(1L)).thenReturn(List.of(a));
        assertThat(as.applicants(2L, 1L)).hasSize(1);

        when(applicationRepository.findById(5L)).thenReturn(Optional.of(a));
        assertThatThrownBy(() -> as.cvOfApplication(new UserPrincipal(4L, "x", Role.EMPLOYER), 5L))
                .hasMessage("Bạn không có quyền xem CV này");
        assertThatThrownBy(() -> as.cvOfApplication(new UserPrincipal(2L, "x", Role.EMPLOYER), 5L))
                .hasMessage("Đơn này không đính kèm CV");
    }

    @Test
    void rutDonDangChoDuyet_thanhCong() {
        ApplicationService as = applicationService(null);
        Application a = application(5, job(1, employer, JobStatus.OPEN, 2), student, ApplicationStatus.PENDING);
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(a));
        assertThatThrownBy(() -> as.withdraw(99L, 5L)).hasMessage("Bạn không có quyền với đơn này");
        as.withdraw(3L, 5L);
        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    // ---------- ReviewService ----------

    @Test
    void ntdDanhGiaSinhVienDaLamViec_thanhCong_vaTinhDiemTrungBinh() {
        ReviewService rs = new ReviewService(reviewRepository, applicationRepository, userRepository,
                jobService(profileService(null)), notifications());
        JobPost job = job(1, employer, JobStatus.COMPLETED, 1);
        when(jobPostRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobIdAndStudentId(1L, 3L))
                .thenReturn(Optional.of(application(5, job, student, ApplicationStatus.ACCEPTED)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(employer));
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        var item = rs.create(2L, 1L, new ReviewRequest(3L, 5, "Nhiệt tình"));
        assertThat(item.rating()).isEqualTo(5);

        when(reviewRepository.averageRating(3L)).thenReturn(4.666);
        when(reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(3L)).thenReturn(List.of());
        assertThat(rs.ofUser(3L).avgRating()).isEqualTo(4.7);
    }

    // ---------- AdminUserService ----------

    @Test
    void adminKhoaTaiKhoan_vaKhongKhoaDuocAdmin() {
        AdminUserService au = new AdminUserService(userRepository);
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));
        assertThat(au.setStatus(1L, 3L, UserStatus.LOCKED).status()).isEqualTo("LOCKED");

        User admin = user(1, Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> au.setStatus(1L, 1L, UserStatus.LOCKED)).hasMessage("Không thể khóa tài khoản quản trị viên");

        when(userRepository.search(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(student)));
        assertThat(au.search(" ", null, null, 0).items()).hasSize(1);
    }

    @Test
    void layThongTinTaiKhoanDangNhap() {
        AuthService as = new AuthService(userRepository, studentProfileRepository, employerProfileRepository, null, null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(student));
        assertThat(as.me(3L).role()).isEqualTo("STUDENT");
    }
}
