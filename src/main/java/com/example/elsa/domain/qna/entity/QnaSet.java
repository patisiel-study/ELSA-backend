package com.example.elsa.domain.qna.entity;

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

    @Column(length = 2000)
    private String question;
    @Column(length = 2000)
    private String answer;

    private double sentimentScore;

    private boolean ethicalPass;


    public QnaSet(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
        this.ethicalPass = sentimentScore < 0;
    }
}
