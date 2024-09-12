package com.example.elsa.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisSubmitResponse {
    private List<StandardScoreDto> standardScoreList;
    private List<NoOrNotApplicableDto> noOrNotApplicableList;
    private TotalScoreDto totalScoreDto;
}
