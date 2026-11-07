package vn.vlu.svjob.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vlu.svjob.dto.AuthDtos.UserResponse;
import vn.vlu.svjob.dto.CommonDtos.PageResponse;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.entity.UserStatus;
import vn.vlu.svjob.exception.ApiException;
import vn.vlu.svjob.repository.UserRepository;

/** Quản lý người dùng: tìm, khóa / mở khóa (FR-20). */
@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(String keyword, Role role, UserStatus status, int page) {
        String kw = AuthService.blankToNull(keyword);
        return PageResponse.of(userRepository.search(kw, role, status, PageRequest.of(Math.max(page, 0), 20))
                .map(UserResponse::from));
    }

    @Transactional
    public UserResponse setStatus(Long adminId, Long userId, UserStatus status) {
        User u = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Không tìm thấy tài khoản"));
        if (u.getId().equals(adminId) || u.getRole() == Role.ADMIN) {
            throw ApiException.conflict("Không thể khóa tài khoản quản trị viên");
        }
        u.setStatus(status);
        return UserResponse.from(u);
    }
}
