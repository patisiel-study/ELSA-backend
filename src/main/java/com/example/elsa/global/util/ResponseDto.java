package com.example.elsa.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private final String message;
    private final T data;

    public ResponseDto(String message) {
        this.message = message;
        this.data = null;
    }
}