package com.example.elsa.domain.diagnosis.dto;

import com.example.elsa.domain.member.entity.Career;
import com.example.elsa.domain.member.entity.Country;
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
    private String llmName;
    private List<StandardScoreDto> standardScoreList;
    private List<NoOrNotApplicableDto> noOrNotApplicableList;
    private TotalScoreDto totalScoreDto;
    private Career career;
    private Country country;
}
