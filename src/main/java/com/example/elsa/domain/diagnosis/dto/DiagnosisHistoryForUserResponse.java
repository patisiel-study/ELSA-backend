package com.example.elsa.domain.diagnosis.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisHistoryForUserResponse {
	private Long diagnosisId;
	private LocalDateTime createdAt;
	private Double totalScore;
	private String totalScoreToString;
}