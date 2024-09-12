package com.example.elsa.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MemberDiagnosisListDto {
    private Long id;
    private LocalDateTime createdAt;
    private String totalScoreString;
    private double totalScoreDouble;
}
