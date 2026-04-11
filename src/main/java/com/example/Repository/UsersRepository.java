package com.example.Repository;

import com.example.Entity.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    // Tìm người dùng bằng username hay email
    Optional<Users> findByUserNameOrEmail(String userName, String email);

    // Kiểm tra xem username đã tồn tại chưa (dùng cho lúc Register)
    Boolean existsByUserName(String userName);

    // Kiểm tra email đã tồn tại chưa
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM Users u WHERE u.created >= :dateLimit ORDER BY u.created DESC")
    List<Users> findRecentUsers(@Param("dateLimit") java.time.LocalDateTime dateLimit);

    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = :roleId", nativeQuery = true)
    long countUsersByRoleId(@Param("roleId") int roleId);

    @Query(value = "SELECT u.* FROM users u JOIN user_roles ur ON u.id = ur.user_id WHERE ur.role_id = 2", nativeQuery = true)
    List<Users> findAllStudents();
}
