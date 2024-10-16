package com.example.elsa.domain.diagnosis.dto;

import com.example.elsa.domain.member.entity.Career;
import com.example.elsa.domain.member.entity.Country;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NonMemberDiagnosisSubmitRequest {
    private List<AnswerDto> answers;
    private Career career;
    private Country country;
    private String llmName;
}
