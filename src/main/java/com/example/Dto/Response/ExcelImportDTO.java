package com.example.Dto.Response;

import lombok.Data;

@Data
public class ExcelImportDTO {
    private String expression;
    private String kana;
    private String romaji;
    private String meaning;
    private String imageFileName;
    private String audioFileName;
    private String wordType;
    private String example;
    private String exampleVi;
}