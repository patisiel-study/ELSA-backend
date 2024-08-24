package com.example.elsa.domain.diagnosis.entity;

public enum Answer {
    YES("YES"),
    NO("NO"),
    NOT_APPLICABLE("미해당"),
    NOT_ANSWERED("미실시");

    private String description;

    Answer(String description) {
        this.description = description;
    }
}
