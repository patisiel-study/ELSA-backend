package com.example.elsa.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotalScoreDto {
    private String scoreRatioString;
    private double scoreRatio;
}
