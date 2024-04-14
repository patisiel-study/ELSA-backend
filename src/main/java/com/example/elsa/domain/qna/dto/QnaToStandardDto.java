package com.example.elsa.domain.qna.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class QnaToStandardDto {
    private List<String> standardNameList;
    private String question;
}
