package com.example.Repository;

import com.example.Entity.UserTestBest;
import com.example.Entity.UserTestBestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserTestBestRepository extends JpaRepository<UserTestBest, UserTestBestId> {

    Optional<UserTestBest> findByIdUserIdAndIdTestId(Integer userId, Integer testId);

    @Query("SELECT utb FROM UserTestBest utb WHERE utb.id.userId = :userId AND utb.id.testId = :testId")
    Optional<UserTestBest> findByUserIdAndTestId(@Param("userId") Integer userId, @Param("testId") Integer testId);

    @Query("SELECT utb.bestScore FROM UserTestBest utb WHERE utb.id.userId = :userId AND utb.id.testId = :testId")
    Optional<Integer> findBestScoreByUserAndTest(@Param("userId") Integer userId, @Param("testId") Integer testId);

    @Modifying
    @Transactional
    @Query("UPDATE UserTestBest utb SET utb.bestScore = :bestScore, utb.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE utb.id.userId = :userId AND utb.id.testId = :testId")
    int updateBestScore(@Param("userId") Integer userId, @Param("testId") Integer testId,
                        @Param("bestScore") Integer bestScore);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserTestBest utb WHERE utb.id.userId = :userId AND utb.id.testId = :testId")
    void deleteByUserIdAndTestId(@Param("userId") Integer userId, @Param("testId") Integer testId);
}