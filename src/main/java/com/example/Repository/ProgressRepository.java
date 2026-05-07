package com.example.Repository;

import com.example.Entity.Progress;
import com.example.Entity.Progress.ProgressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, ProgressId> {

    @Query("SELECT p FROM Progress p WHERE p.id.userId = :userId AND p.id.vocabId IN :vocabIds")
    List<Progress> findByUserIdAndVocabIdIn(@Param("userId") Integer userId,
                                            @Param("vocabIds") List<Integer> vocabIds);

    @Query("SELECT p FROM Progress p WHERE p.id.userId = :userId AND p.isLearned = true")
    List<Progress> findLearnedByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.id.userId = :userId AND p.isLearned = true")
    long countLearnedByUserId(@Param("userId") Integer userId);
}