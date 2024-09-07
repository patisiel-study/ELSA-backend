package com.example.elsa.domain.qna.entity;

import com.example.elsa.domain.qna.enums.LLMModel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100000)
    private String question;
    @Column(length = 100000)
    private String answer;

    @Enumerated(EnumType.STRING)
    private LLMModel model;

    private double sentimentScore;

    private boolean ethicalPass;


    public QnaSet(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
    public QnaSet(String question, String answer, LLMModel model) {
        this.question = question;
        this.answer = answer;
        this.model = model;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
        this.ethicalPass = sentimentScore < 0;
    }

    // setModel method 추가
    public void setModel(LLMModel model) {
        this.model = model;
    }
}