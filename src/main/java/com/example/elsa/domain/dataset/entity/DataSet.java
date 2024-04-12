package com.example.elsa.domain.dataset.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    private List<String> keywords;

    public DataSet(String name) {
        this.name = name;
        this.keywords = new ArrayList<>();
    }

    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }
}
