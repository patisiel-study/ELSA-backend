package com.example.elsa.domain.diagnosis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SubmitDiagnosisRequest {
    private Long diagnosisId;

    public SubmitDiagnosisRequest(Long diagnosisId) {
        this.diagnosisId = diagnosisId;
    }
}
