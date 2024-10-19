package com.example.elsa.domain.diagnosis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DiagnosisSubmitRequest {
    private List<AnswerDto> answers;
}
