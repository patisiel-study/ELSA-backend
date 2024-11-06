package com.example.elsa.domain.diagnosis.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NonMemberDiagnosisForUserSubmitRequest {
	private List<AnswerDto> answers;
	private String career;
	private String country;
}
