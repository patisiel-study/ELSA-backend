package com.example.elsa.domain.member.entity;

public enum Career {
	NORMAL("일반"), STUDENT("학생"), DEVELOPER("개발자"), COMPANY("기업"), ETC("기타");

	private final String description;

	Career(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static Career fromDescription(String description) {
		for (Career career : Career.values()) {
			if (career.getDescription().equals(description)) {
				return career;
			}
		}
		throw new IllegalArgumentException("No matching career for description: " + description);
	}
}
