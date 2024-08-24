package com.example.elsa.domain.qna.dto;

import lombok.Getter;

@Getter
public class QnaToDeleteRequest {
    private String standardName;
    private Long qnaSetId;
}
