package com.example.elsa.domain.qna.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Standard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(length = 1000)
	private String description;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "standard_id")
	private List<QnaSet> qnaSetList = new ArrayList<>();

	public Standard(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public void addQnaSet(QnaSet qnaSet) {
		qnaSetList.add(qnaSet);
	}

	public void removeQnaSet(QnaSet qnaSet) {
		qnaSetList.remove(qnaSet);
	}
}