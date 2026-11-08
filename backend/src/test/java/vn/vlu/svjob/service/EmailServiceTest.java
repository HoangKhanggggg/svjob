package vn.vlu.svjob.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Email "đơn được nhận" (góp ý giảng viên). */
class EmailServiceTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);

    @Test
    void noiDungThuCoTenSinhVienViTriVaCongTy() {
        var svc = new EmailService(provider, false, "a@b.c", "http://localhost:5173");
        String body = svc.acceptedBody("Nguyễn Văn An", "Phục vụ ca tối", "The Coffee Corner");
        assertThat(body).contains("Nguyễn Văn An", "Phục vụ ca tối", "The Coffee Corner", "/student/applications");
    }

    @Test
    void tatMail_khongDungDenSmtp() {
        var svc = new EmailService(provider, false, "a@b.c", "http://localhost:5173");
        svc.sendApplicationAccepted("sv@vlu.vn", "An", "Job", "Cty");
        verifyNoInteractions(provider);
    }

    @Test
    void batMail_guiMotThuDenDungNguoiNhan() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        var svc = new EmailService(provider, true, "a@b.c", "http://localhost:5173");
        svc.sendApplicationAccepted("sv@vlu.vn", "An", "Job", "Cty");
        verify(sender, timeout(2000)).send(argThat((org.springframework.mail.SimpleMailMessage m) ->
                m.getTo() != null && m.getTo()[0].equals("sv@vlu.vn") && m.getSubject().contains("Job")));
    }

    @Test
    void smtpLoi_khongNemNgoaiLe() {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        doThrow(new RuntimeException("SMTP down")).when(sender).send(any(org.springframework.mail.SimpleMailMessage.class));
        var svc = new EmailService(provider, true, "a@b.c", "http://localhost:5173");
        svc.sendApplicationAccepted("sv@vlu.vn", "An", "Job", "Cty");
        verify(sender, timeout(2000)).send(any(org.springframework.mail.SimpleMailMessage.class));
    }
}
