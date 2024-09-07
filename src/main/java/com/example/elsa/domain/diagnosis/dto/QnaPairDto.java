package com.example.elsa.domain.diagnosis.dto;

import com.example.elsa.domain.diagnosis.entity.Answer;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaPairDto {
    private String question;
    @Enumerated(EnumType.STRING)
    private Answer answer;
}
