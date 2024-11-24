package com.example.elsa.domain.diagnosis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class NonMemberSubmitDiagnosisResponse {
	private Long diagnosisId;

	public NonMemberSubmitDiagnosisResponse(Long diagnosisId) {
		this.diagnosisId = diagnosisId;
	}
}