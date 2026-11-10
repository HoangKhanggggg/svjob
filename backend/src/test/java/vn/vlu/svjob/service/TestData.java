package vn.vlu.svjob.service;

import vn.vlu.svjob.dto.JobDtos.JobRequest;
import vn.vlu.svjob.entity.*;

import java.time.LocalDate;

/** Tạo nhanh đối tượng mẫu cho unit test. */
final class TestData {

    private TestData() {
    }

    static User user(long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail("user" + id + "@vlu.vn");
        u.setFullName("User " + id);
        u.setRole(role);
        return u;
    }

    static JobPost job(long id, User employer, JobStatus status, int quantity) {
        JobCategory c = new JobCategory();
        c.setId(1);
        c.setName("Phục vụ - F&B");
        JobPost j = new JobPost();
        j.setId(id);
        j.setEmployer(employer);
        j.setCategory(c);
        j.setTitle("Phục vụ quán cà phê");
        j.setDescription("Mô tả");
        j.setSalaryMin(25000);
        j.setSalaryUnit(SalaryUnit.HOUR);
        j.setLocation("Bình Thạnh");
        j.setWorkTime("Tối");
        j.setQuantity(quantity);
        j.setDeadline(LocalDate.now().plusDays(10));
        j.setStatus(status);
        return j;
    }

    static Application application(long id, JobPost job, User student, ApplicationStatus status) {
        Application a = new Application();
        a.setId(id);
        a.setJob(job);
        a.setStudent(student);
        a.setStatus(status);
        return a;
    }

    static JobRequest jobRequest(int min, Integer max) {
        return new JobRequest("Phục vụ", 1, "Mô tả", min, max, SalaryUnit.HOUR, "Bình Thạnh", "Tối", 2,
                LocalDate.now().plusDays(10));
    }
}
