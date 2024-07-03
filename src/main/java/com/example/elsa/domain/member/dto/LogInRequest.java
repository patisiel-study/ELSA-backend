package com.example.elsa.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogInRequest {
    @Schema(description = "이메일", example = "test1234@gmail.com", required = true)
    private String email;
    @Schema(description = "비밀번호", example = "!test1234", required = true)
    private String password;
}