package com.example.Dto.Response;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private long totalUsers;
    private long totalLessons;
    private long totalVocab;
    private long totalCategories;
}
