package com.example.elsa.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisHistoryResponse {
    private Long diagnosisId;
    private LocalDateTime createdAt;
    private Double totalScore;
    private String totalScoreToString;
    private String llmName;
}