package com.example.Dto.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer id;
    private String categoryName;
    private String slug;
    private String jlptLevel;
    private Boolean isActive;
}