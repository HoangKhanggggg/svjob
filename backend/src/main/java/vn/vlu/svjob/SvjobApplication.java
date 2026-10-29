package vn.vlu.svjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SvjobApplication {

    public static void main(String[] args) {
        // Giờ Việt Nam cho mọi thời điểm lưu vào CSDL, kể cả khi deploy lên server đặt giờ UTC
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(SvjobApplication.class, args);
    }
}
