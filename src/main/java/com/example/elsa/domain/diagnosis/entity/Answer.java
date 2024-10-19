package com.example.elsa.domain.diagnosis.entity;

public enum Answer {
	YES("예"),
	NO("아니오"),
	NOT_APPLICABLE("해당없음"),
	NOT_ANSWERED("미실시");

	private final String description;

	Answer(String description) {
		this.description = description;
	}
}
