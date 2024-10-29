package com.example.elsa.domain.diagnosis.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisSubmitResponse {
	private String llmName;
	private List<StandardScoreDto> standardScoreList;
	private List<NoOrNotApplicableDto> noOrNotApplicableList;
	private TotalScoreDto totalScoreDto;
	private String career;
	private String country;
}
