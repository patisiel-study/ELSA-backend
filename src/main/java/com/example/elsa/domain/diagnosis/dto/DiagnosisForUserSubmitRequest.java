package com.example.elsa.domain.diagnosis.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiagnosisForUserSubmitRequest {
	private List<AnswerDto> answers;
}
