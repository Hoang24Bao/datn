package com.example.Repository;

import com.example.Entity.Kanji;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KanjiRepository extends JpaRepository<Kanji, Integer> {

    @Query("SELECT k FROM Kanji k WHERE " +
            "(:level IS NULL OR k.jlptLevel = :level) AND " +
            "(:search IS NULL OR LOWER(k.character) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(k.meaning) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(k.sinoVietnamese) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +  // Đổi tên
            "k.isActive = true")
    Page<Kanji> findWithFilters(@Param("level") String level,
                                @Param("search") String search,
                                Pageable pageable);
}