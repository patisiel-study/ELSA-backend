package com.example.elsa.domain.qna.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaSet {
    @Column(length = 1000)
    private String question;
    @Column(length = 1000)
    private String answer;

    public QnaSet(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}
