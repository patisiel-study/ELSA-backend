package com.example.elsa.domain.diagnosis.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NoOrNotApplicableDto {
	private String standardName;
	private String description;
	private List<QnaPairDto> qnaPairDtoList;
}
