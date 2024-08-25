package com.example.elsa.domain.diagnosis.dto;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StandardQuestionsDto {
    private String standardName;
    private List<QuestionDto> questions;

    @Getter
    @Builder
    public static class QuestionDto {
        private Long questionId;
        private String question;

        public QuestionDto(Long questionId, String question) {
            this.questionId = questionId;
            this.question = question;
        }
    }
}
