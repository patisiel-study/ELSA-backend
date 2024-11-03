package com.example.elsa.domain.diagnosis.entity;

import com.example.elsa.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DiagnosisType diagnosisType;

	// totalScore를 변경하는 메서드
	public void updateTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

	public static Diagnosis createDiagnosis(Long memberId, Double totalScore, String llmName) {
		return Diagnosis.builder()
			.memberId(memberId)
			.totalScore(totalScore)
			.llmName(llmName)
			.diagnosisType(DiagnosisType.DEVELOPER)
			.build();
	}

	public static Diagnosis createDiagnosisForUser(Long memberId, Double totalScore) {
		return Diagnosis.builder()
			.memberId(memberId)
			.totalScore(totalScore)
			.llmName("user mode")
			.diagnosisType(DiagnosisType.USER)
			.build();
	}

	public void updateTotalScoreToString(String ratioString) {
		this.totalScoreToString = ratioString;
	}
}
