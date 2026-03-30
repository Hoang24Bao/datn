package com.example.Repository;

import com.example.Entity.Users;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    // Tìm người dùng bằng tên đăng nhập (dùng cho lúc Login)
    Optional<Users> findByUserName(String userName);

    // Kiểm tra xem username đã tồn tại chưa (dùng cho lúc Register)
    Boolean existsByUserName(String userName);

    // Kiểm tra email đã tồn tại chưa
    Boolean existsByEmail(String email);
}
