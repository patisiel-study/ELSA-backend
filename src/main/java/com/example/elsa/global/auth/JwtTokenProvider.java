package com.example.elsa.global.auth;

import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds = 60 * 60 * 1000L; // 1시간
    private final long refreshTokenValidityInMilliseconds = 14 * 24 * 60 * 60 * 1000L; // 2주

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final CustomUserDetailsService customUserDetailsService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Member 정보 가지고 AccessToken, RefreshToken 생성
    public JwtToken generateToken(Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpireIn = new Date(now + accessTokenValidityInMilliseconds);
        String accessToken = buildToken(authentication.getName(), authorities, accessTokenExpireIn);

        // Refresh Token 생성
        Date refreshTokenExpireIn = new Date(now + refreshTokenValidityInMilliseconds);
        String refreshToken = buildToken(authentication.getName(), null, refreshTokenExpireIn);

        // Refresh Token을 Redis에 저장
        storeRefreshToken(authentication.getName(), refreshToken);

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼냄
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        CustomUserDetails principal = customUserDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.info("Invalid JWT Token: {}", e.getMessage());
            return false;
        }
    }

    // AccessToken 갱신
    public JwtToken refreshAccessToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        String studentId = claims.getSubject();

        // RT 토큰 유효성 확인
        validateRefreshToken(refreshToken, studentId);

        String authorities = getUserAuthorities(studentId);
        long now = (new Date()).getTime();
        Date accessTokenExpireIn = new Date(now + accessTokenValidityInMilliseconds);
        String newAccessToken = buildToken(studentId, authorities, accessTokenExpireIn);

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void validateRefreshToken(String refreshToken, String username) {
        if (username == null || username.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String savedRefreshToken = (String) redisTemplate.opsForValue().get(username);

        if (savedRefreshToken == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_REFRESH_TOKEN);
        }

        if (!refreshToken.equals(savedRefreshToken)) {
            throw new CustomException(ErrorCode.NOT_MATCHED_REFRESH_TOKEN);
        }

        Claims claims = parseClaims(refreshToken);
        if (claims.getExpiration().before(new Date())) {
            redisTemplate.delete(username);
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
    }

    private String buildToken(String subject, String authorities, Date expiration) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setExpiration(expiration)
                .signWith(key);

        if (authorities != null) {
            builder.claim("auth", authorities);
        }

        return builder.compact();
    }

    private void storeRefreshToken(String studentId, String refreshToken) {
        try {
            redisTemplate.opsForValue().set(studentId, refreshToken, refreshTokenValidityInMilliseconds, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failed to store Refresh Token in Redis", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String getUserAuthorities(String username) {
        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()  // 첫 번째 권한을 가져옵니다.
                .orElseThrow(() -> new CustomException(ErrorCode.NO_AUTHORITY_FOUND));  // 권한이 없을 경우 예외를 던집니다.
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Redis에 저장된 RefreshToken을 삭제
    public void deleteRefreshToken(String studentId) {
        redisTemplate.delete(studentId);
    }
}
