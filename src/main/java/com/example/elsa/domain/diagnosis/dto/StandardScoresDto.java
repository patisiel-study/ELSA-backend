package com.example.elsa.domain.diagnosis.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StandardScoresDto {
    private String name;
    private Double score;
}
