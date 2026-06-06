package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "User_Test_Best")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTestBest {

    @EmbeddedId
    private UserTestBestId id;

    @Column(name = "best_score", nullable = false)
    private Integer bestScore = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

}