package com.example.Service;

import com.example.Dto.Response.CategoryDTO;
import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoriesRepository categoriesRepository;

    public List<CategoryDTO> getAllActiveCategories() {
        List<Categories> categories = categoriesRepository.findByIsActiveTrue();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Categories category) {
        return new CategoryDTO(
                category.getId(),
                category.getCategoryName(),
                category.getSlug(),
                category.getJlptLevel(),
                category.getIsActive()
        );
    }
}