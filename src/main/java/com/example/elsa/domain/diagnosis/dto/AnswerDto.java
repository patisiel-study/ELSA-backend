package com.example.elsa.domain.diagnosis.dto;

import com.example.elsa.domain.diagnosis.entity.Answer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerDto {
    private Long questionId;
    private Answer answer;
}
