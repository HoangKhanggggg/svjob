package vn.vlu.svjob.security;

import vn.vlu.svjob.entity.Role;

/** Thông tin người đang đăng nhập, lấy trong Controller bằng @AuthenticationPrincipal. */
public record UserPrincipal(Long id, String email, Role role) {
}
