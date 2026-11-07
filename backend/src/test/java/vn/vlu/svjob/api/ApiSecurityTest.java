package vn.vlu.svjob.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm thử tích hợp nhỏ: chạy cả ứng dụng với H2, gọi API thật qua MockMvc. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityTest {

    @Autowired MockMvc mvc;

    @Test
    void dangKyRoiDangNhap_nhanDuocToken_vaSinhVienKhongDangTinDuoc() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@vlu.vn\",\"password\":\"Abc12345\",\"fullName\":\"Test\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isCreated());

        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@vlu.vn\",\"password\":\"Abc12345\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/api/jobs").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void khongCoToken_goiApiCanDangNhap_bao401() throws Exception {
        mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
    }

    @Test
    void matKhauNgan_bao400KemLoiTheoTruong() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@vlu.vn\",\"password\":\"1234\",\"fullName\":\"A\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Mật khẩu tối thiểu 8 ký tự"));
    }

    @Test
    void danhSachTinCongKhai_khongCanDangNhap() throws Exception {
        mvc.perform(get("/api/jobs")).andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void thongKeTrangChu_congKhai() throws Exception {
        mvc.perform(get("/api/stats")).andExpect(status().isOk()).andExpect(jsonPath("$.categories").isArray());
    }
}
