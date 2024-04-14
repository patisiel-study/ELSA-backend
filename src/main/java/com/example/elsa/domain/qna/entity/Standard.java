package com.example.elsa.domain.qna.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Standard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "qna_set", joinColumns = @JoinColumn(name = "standard_id"))
    private List<QnaSet> qnaSetList = new ArrayList<>();

    public Standard(String name) {
        this.name = name;
    }

    public void addQnaSet(String question, String answer) {
        QnaSet data = new QnaSet(question, answer);
        qnaSetList.add(data);
    }
}
