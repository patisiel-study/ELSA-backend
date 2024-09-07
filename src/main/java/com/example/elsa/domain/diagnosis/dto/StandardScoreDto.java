package com.example.elsa.domain.diagnosis.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StandardScoreDto {
    private String standardName;
    private Integer score;
}
