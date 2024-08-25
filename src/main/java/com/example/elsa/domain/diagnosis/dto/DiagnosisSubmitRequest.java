package com.example.elsa.domain.diagnosis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiagnosisSubmitRequest {
    private List<AnswerDto> answers;
}
