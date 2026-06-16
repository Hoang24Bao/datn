package com.example.Repository;

import com.example.Entity.Kanji;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KanjiRepository extends JpaRepository<Kanji, Integer> {

    @Query(value = "SELECT * FROM Kanji k WHERE " +
            "(:level IS NULL OR k.jlpt_level = :level) AND " +
            "(:search IS NULL OR " +
            "k.character LIKE CONCAT('%', :search, '%') OR " +  // Dùng COLLATE cho SQL Server
            "LOWER(k.meaning) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(k.sino_vietnamese) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "k.is_active = 1",
            nativeQuery = true)
    Page<Kanji> findWithFilters(@Param("level") String level,
                                @Param("search") String search,
                                Pageable pageable);
}