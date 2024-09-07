package com.example.elsa.domain.qna.enums;

public enum LLMModel {
    GPT_3_5("gpt-3.5-turbo"),
    GPT_4("gpt-4"),
    GPT_4o("gpt-4o"),
    GEMINI;

    private final String modelName;

    LLMModel() {
        this.modelName = this.name();
    }

    LLMModel(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

}