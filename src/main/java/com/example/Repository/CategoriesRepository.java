package com.example.Repository;

import com.example.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    // JpaRepository đã có sẵn các hàm findAll(), findById()
}