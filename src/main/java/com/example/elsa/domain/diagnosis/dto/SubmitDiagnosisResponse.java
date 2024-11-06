package com.example.elsa.domain.diagnosis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SubmitDiagnosisResponse {
	private Long diagnosisId;

	public SubmitDiagnosisResponse(Long diagnosisId) {
		this.diagnosisId = diagnosisId;
	}
}
