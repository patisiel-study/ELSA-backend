package com.example.elsa.domain.diagnosis.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NoOrNotApplicableDto {
    private String standardName;
    private List<QnaPairDto> qnaPairDtoList;
}
