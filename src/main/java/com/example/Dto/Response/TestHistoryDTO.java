package com.example.Dto.Response;

import java.time.LocalDateTime;

public interface TestHistoryDTO {
    Integer getId();

    Integer getTestId();

    String getTestTitle();

    String getCategoryName();

    Integer getTotalCount();

    Integer getDurationSeconds();

    Float getScore();

    Integer getMaxScore();

    LocalDateTime getCompletedAt();

    Boolean getIsPassed();
}
