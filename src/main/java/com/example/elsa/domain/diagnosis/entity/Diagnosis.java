package com.example.elsa.domain.diagnosis.entity;

import com.example.elsa.global.common.BaseEntity;
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
public class Diagnosis extends BaseEntity {
    @Id
    @Column(name = "diagnosis_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diagnosisId;

    @Column(nullable = false)
    private Double totalScore;

    @Column
    private String totalScoreToString;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String llmName;

    // totalScore를 변경하는 메서드
    public void updateTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public static Diagnosis createDiagnosis(Long memberId, Double totalScore, String llmName) {
        return Diagnosis.builder()
                .memberId(memberId)
                .totalScore(totalScore)
                .llmName(llmName)
                .build();
    }

    public void updateTotalScoreToString(String ratioString) {
        this.totalScoreToString = ratioString;
    }
}
