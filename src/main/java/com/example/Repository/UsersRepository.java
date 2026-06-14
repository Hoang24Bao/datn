package com.example.Repository;

import com.example.Entity.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    // Tìm người dùng bằng username hay email
    Optional<Users> findByUserNameOrEmail(String userName, String email);

    Optional<Users> findByUserName(String userName);

    // Kiểm tra xem username đã tồn tại chưa (lúc Register)
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


    @Query(value = "SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = 2 " +
            "AND (:levelId IS NULL OR u.level_id = :levelId) " +
            "AND (:active IS NULL OR u.active = :active) " +
            "AND (:search IS NULL OR :search = '' OR (u.user_name LIKE %:search% OR u.fullname LIKE %:search% OR u.email LIKE %:search%)) " +
            "AND (:dateLimit IS NULL OR u.created >= :dateLimit)",
            countQuery = "SELECT COUNT(u.id) FROM users u " +
                    "JOIN user_roles ur ON u.id = ur.user_id " +
                    "WHERE ur.role_id = 2 " +
                    "AND (:levelId IS NULL OR u.level_id = :levelId) " +
                    "AND (:active IS NULL OR u.active = :active) " +
                    "AND (:search IS NULL OR :search = '' OR (u.user_name LIKE %:search% OR u.fullname LIKE %:search% OR u.email LIKE %:search%)) " +
                    "AND (:dateLimit IS NULL OR u.created >= :dateLimit)",
            nativeQuery = true)
    Page<Users> findStudentsWithFilters(
            @Param("levelId") Integer levelId,
            @Param("active") Boolean active,
            @Param("search") String search,
            @Param("dateLimit") java.time.LocalDateTime dateLimit,
            Pageable pageable);
}
