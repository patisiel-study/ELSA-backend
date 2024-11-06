package com.example.elsa.domain.diagnosis.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiagnosisSubmitRequest {
	private List<AnswerDto> answers;
	private String llmName;
}
