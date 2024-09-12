package com.example.elsa.domain.diagnosis.entity;

import com.example.elsa.domain.diagnosis.dto.StandardQuestionsDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_qna_set_id")
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String standardName;

    public StandardQuestionsDto.QuestionDto toDto() {
        return StandardQuestionsDto.QuestionDto.builder()
                .questionId(this.id)
                .question(this.question)
                .build();
    }
}
