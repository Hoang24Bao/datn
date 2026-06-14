package com.example.Repository;

import com.example.Entity.Progress;
import com.example.Entity.Progress.ProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, ProgressId> {

    // Tìm progress theo user_id và vocab_id
    @Query("SELECT p FROM Progress p WHERE p.id.userId = :userId AND p.id.vocabId = :vocabId")
    Optional<Progress> findByUserIdAndVocabId(@Param("userId") Integer userId,
                                              @Param("vocabId") Integer vocabId);

    // Tìm nhiều progress theo user_id và danh sách vocab_id
    @Query("SELECT p FROM Progress p WHERE p.id.userId = :userId AND p.id.vocabId IN :vocabIds")
    List<Progress> findByUserIdAndVocabIdIn(@Param("userId") Integer userId,
                                            @Param("vocabIds") List<Integer> vocabIds);

    // Tìm tất cả progress của user
    @Query("SELECT p FROM Progress p WHERE p.id.userId = :userId")
    List<Progress> findByUserId(@Param("userId") Integer userId);

    // Cập nhật memory_level, correct_streak và last_reviewed
    @Modifying
    @Transactional
    @Query("UPDATE Progress p SET " +
            "p.memoryLevel = :memoryLevel, " +
            "p.correctStreak = :correctStreak, " +
            "p.isLearned = :isLearned, " +
            "p.lastReviewed = CURRENT_TIMESTAMP " +
            "WHERE p.id.userId = :userId AND p.id.vocabId = :vocabId")
    void updateProgress(@Param("userId") Integer userId,
                        @Param("vocabId") Integer vocabId,
                        @Param("memoryLevel") Integer memoryLevel,
                        @Param("correctStreak") Integer correctStreak,
                        @Param("isLearned") Boolean isLearned);

    @Query("SELECT AVG(p.memoryLevel) FROM Progress p WHERE p.id.userId = :userId")
    Double getAverageMemoryLevelByUserId(@Param("userId") Integer userId);
}