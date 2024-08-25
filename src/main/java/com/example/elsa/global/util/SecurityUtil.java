package com.example.elsa.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    // 로그인 된 회원의 이메일 반환
    public static String getCurrentMemberEmail() {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authentication information.");
        }
        return authentication.getName();
    }
}
