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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "standard_id")
    private List<QnaSet> qnaSetList = new ArrayList<>();

    public Standard(String name) {
        this.name = name;
    }

    public void addQnaSet(QnaSet qnaSet) {
        qnaSetList.add(qnaSet);
    }

    public void removeQnaSet(QnaSet qnaSet) {
        qnaSetList.remove(qnaSet);
    }
}