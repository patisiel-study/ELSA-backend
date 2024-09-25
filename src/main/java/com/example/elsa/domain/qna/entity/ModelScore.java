package com.example.elsa.domain.qna.entity;

import com.example.elsa.domain.qna.enums.LLMModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ModelScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "standard_id")
    private Standard standard;

    @Enumerated(EnumType.STRING)
    private LLMModel model;

    private double score;


    public ModelScore(Standard standard, LLMModel model, double score) {
        this.standard = standard;
        this.model = model;
        this.score = score;
    }
}