package com.example.elsa.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotalScore {
    private String ratioString;
    private double ratioDouble;
}
