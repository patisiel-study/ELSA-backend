package com.example.elsa.domain.diagnosis.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StandardQuestionsDto {
	private String standardName;
	private String description;
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
