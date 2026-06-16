package com.example.Repository;

import com.example.Entity.UserCategoryUnlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCategoryUnlockRepository extends JpaRepository<UserCategoryUnlock, Integer> {

    Optional<UserCategoryUnlock> findByUserIdAndCategoryId(Integer userId, Integer categoryId);

    List<UserCategoryUnlock> findByUserId(Integer userId);

    boolean existsByUserIdAndCategoryId(Integer userId, Integer categoryId);

    @Query("SELECT u.categoryId FROM UserCategoryUnlock u WHERE u.userId = :userId")
    List<Integer> findUnlockedCategoryIdsByUserId(@Param("userId") Integer userId);
}