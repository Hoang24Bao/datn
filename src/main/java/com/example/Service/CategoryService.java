package com.example.Service;

import com.example.Dto.Response.CategoryDTO;
import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.TestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private TestRepository testRepository;

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

    public List<Categories> getAllCategoriesOrdered() {
        return categoriesRepository.findAllByOrderByIdAsc();
    }

    /**
     * Thêm category mới và tự động cập nhật next_category_id
     */
    @Transactional
    public Categories createCategory(Categories newCategory) {
        // 1. Lưu category mới
        Categories savedCategory = categoriesRepository.save(newCategory);

        // 2. Cập nhật next_category_id cho category trước đó
        updatePreviousCategoryNextId(savedCategory.getId());

        // 3. Cập nhật next_category_id cho category hiện tại
        updateCurrentCategoryNextId(savedCategory.getId());

        return savedCategory;
    }

    /**
     * Cập nhật next_category_id cho category trước đó
     */
    private void updatePreviousCategoryNextId(Integer newCategoryId) {
        List<Categories> previousCats = categoriesRepository.findPreviousCategories(newCategoryId);

        if (!previousCats.isEmpty()) {
            Categories previousCategory = previousCats.get(0);
            previousCategory.setNextCategoryId(newCategoryId);
            categoriesRepository.save(previousCategory);
        }
    }

    /**
     * Cập nhật next_category_id cho category hiện tại
     */
    private void updateCurrentCategoryNextId(Integer currentId) {
        List<Categories> nextCats = categoriesRepository.findNextCategories(currentId);
        Categories currentCategory = categoriesRepository.findById(currentId).orElse(null);

        if (currentCategory != null) {
            if (!nextCats.isEmpty()) {
                currentCategory.setNextCategoryId(nextCats.get(0).getId());
            } else {
                currentCategory.setNextCategoryId(null);
            }
            categoriesRepository.save(currentCategory);
        }
    }

    /**
     * Cập nhật lại toàn bộ next_category_id cho tất cả category
     */
    @Transactional
    public void refreshAllNextCategoryIds() {
        List<Categories> allCategories = categoriesRepository.findAllByOrderByIdAsc();

        for (int i = 0; i < allCategories.size(); i++) {
            Categories current = allCategories.get(i);
            Categories next = (i + 1 < allCategories.size()) ? allCategories.get(i + 1) : null;

            current.setNextCategoryId(next != null ? next.getId() : null);
            categoriesRepository.save(current);
        }
    }

    /**
     * Xóa category và cập nhật lại next_category_id
     */
    @Transactional
    public void deleteCategory(Integer id) {
        Categories current = categoriesRepository.findById(id).orElse(null);

        if (current != null) {
            List<Categories> previousCats = categoriesRepository.findPreviousCategories(id);
            List<Categories> nextCats = categoriesRepository.findNextCategories(id);

            // Xóa category
            categoriesRepository.deleteById(id);

            // Cập nhật next_category_id cho category trước đó
            if (!previousCats.isEmpty() && !nextCats.isEmpty()) {
                Categories prev = previousCats.get(0);
                prev.setNextCategoryId(nextCats.get(0).getId());
                categoriesRepository.save(prev);
            } else if (!previousCats.isEmpty()) {
                Categories prev = previousCats.get(0);
                prev.setNextCategoryId(null);
                categoriesRepository.save(prev);
            }

            // Refresh toàn bộ để đảm bảo đồng bộ
            refreshAllNextCategoryIds();
        }
    }

    /**
     * Lấy category tiếp theo dựa vào next_category_id
     */
    public Categories getNextCategory(Integer currentId) {
        Categories current = categoriesRepository.findById(currentId).orElse(null);
        if (current != null && current.getNextCategoryId() != null) {
            return categoriesRepository.findById(current.getNextCategoryId()).orElse(null);
        }
        return null;
    }

    /**
     * Kiểm tra xem category có thể mở khóa không
     */
    public boolean canUnlockCategory(Integer categoryId, Double userAvgScore) {
        Categories category = categoriesRepository.findById(categoryId).orElse(null);
        if (category == null) return false;

        // Category đã mở khóa rồi
        if (!category.getIsLocked()) return true;

        // Kiểm tra điều kiện: điểm trung bình >= unlock_avg_score
        // Và đã hoàn thành tất cả test của category trước
        return userAvgScore >= category.getUnlockAvgScore();
    }


    /**
     * Cập nhật total_tests và unlock_avg_score cho TẤT CẢ category
     */
    @Transactional
    public void updateAllCategoriesStats() {
        List<Categories> allCategories = categoriesRepository.findAllByOrderByIdAsc();

        for (int i = 0; i < allCategories.size(); i++) {
            Categories current = allCategories.get(i);

            // 1. Cập nhật total_tests cho category hiện tại
            Integer totalTests = testRepository.countByCategoryId(current.getId());
            current.setTotalTests(totalTests != null ? totalTests : 0);

            // 2. Cập nhật unlock_avg_score
            if (i == 0) {
                // Category đầu tiên: unlock_avg_score = 0 (mở sẵn)
                current.setUnlockAvgScore(0.0);
            } else {
                // Category còn lại: TỔNG pass_score của category TRƯỚC đó
                Categories previous = allCategories.get(i - 1);
                Double totalScore = testRepository.getSumPassScoreByCategory(previous.getId());
                current.setUnlockAvgScore(totalScore != null ? totalScore : 0.0);
            }

            categoriesRepository.save(current);
        }
    }

    /**
     * Đồng bộ: Cập nhật stats cho category khi có thay đổi về test
     * (Gọi method này trong TestService khi thêm/sửa/xóa test)
     */
    @Transactional
    public void syncCategoryStatsOnTestChange(Integer categoryId) {
        if (categoryId != null) {
            // Cập nhật total_tests cho category này
            Categories category = categoriesRepository.findById(categoryId).orElse(null);
            if (category != null) {
                Integer totalTests = testRepository.countByCategoryId(categoryId);
                category.setTotalTests(totalTests != null ? totalTests : 0);
                categoriesRepository.save(category);
            }

            // Cập nhật unlock_avg_score cho category TIẾP THEO (vì nó phụ thuộc vào category này)
            Categories current = categoriesRepository.findById(categoryId).orElse(null);
            if (current != null && current.getNextCategoryId() != null) {
                Categories next = categoriesRepository.findById(current.getNextCategoryId()).orElse(null);
                if (next != null) {
                    Double totalScore = testRepository.getSumPassScoreByCategory(categoryId);
                    next.setUnlockAvgScore(totalScore != null ? totalScore : 0.0);
                    categoriesRepository.save(next);
                }
            }
        }
    }
}