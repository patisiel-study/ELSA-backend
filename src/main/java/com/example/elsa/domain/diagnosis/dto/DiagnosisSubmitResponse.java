package com.example.elsa.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class DiagnosisSubmitResponse {
    private Map<String, StandardScore> standardScores;
    private Map<String, List<QuestionAnswerPair>> noOrNotApplicableMap;
    private TotalScore totalScore;
}
