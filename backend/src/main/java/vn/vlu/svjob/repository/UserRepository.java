package vn.vlu.svjob.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vlu.svjob.entity.Role;
import vn.vlu.svjob.entity.User;
import vn.vlu.svjob.entity.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    /** Admin tìm tài khoản theo từ khóa (email/họ tên), vai trò, trạng thái – tham số null thì bỏ qua. */
    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<User> search(@Param("keyword") String keyword, @Param("role") Role role,
                      @Param("status") UserStatus status, Pageable pageable);
}
