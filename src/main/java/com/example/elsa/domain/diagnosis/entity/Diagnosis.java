package com.example.elsa.domain.diagnosis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Diagnosis {
    @Id
    @Column(name = "diagnosis_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String standardName;

    @Column(nullable = false)
    private Integer standardScore;

    public static Diagnosis createDiagnosis(String standardName) {
        return Diagnosis.builder()
                .standardName(standardName)
                .standardScore(0)
                .build();
    }
}
