package com.example.elsa.domain.member.controller;

import com.example.elsa.domain.member.dto.LogInRequest;
import com.example.elsa.domain.member.dto.RefreshTokenRequest;
import com.example.elsa.domain.member.dto.SignUpRequest;
import com.example.elsa.domain.member.service.MemberService;
import com.example.elsa.global.auth.CustomUserDetails;
import com.example.elsa.global.auth.JwtToken;
import com.example.elsa.global.auth.JwtTokenProvider;
import com.example.elsa.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "회원 관리 API", description = "회원 관련 API endpoints")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = """
    password: 비밀번호는 최소 8자 이상, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.
    
    role: DEVELOPER, COMPANY 중에서 택 1. 관리자 계정의 경우 ADMIN(ADMIN은 프론트에서 구현되면 안됨)
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    @PostMapping("/signup")
    public ResponseEntity<ResponseDto<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request);
        return ResponseEntity.ok(new ResponseDto<>("회원가입이 완료되었습니다!", null));
    }

    @Operation(summary = "로그인", description = """
    password: 비밀번호는 최소 8자 이상, 영문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.
    
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<JwtToken>> logIn(@Valid @RequestBody LogInRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();


        JwtToken jwtToken = memberService.logIn(email, password);
        log.debug("request email = {}, password = {}", email, password);
        log.debug("jwtToken accessToken = {}, refreshToken = {}", jwtToken.getAccessToken(), jwtToken.getRefreshToken());

        return ResponseEntity.ok(new ResponseDto<>("로그인 성공.", jwtToken));

    }

    @Operation(summary = "Access Token 재발급", description = "Access Token 만료시 기존에 발급받은 Refresh Token을 이쪽으로 보내서 새로운 Access Token 받아가기")
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<?>> refreshAccessToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtToken token = jwtTokenProvider.refreshAccessToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(new ResponseDto<>("Access Token 재발급 완료.", token));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인 된 계정의 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        jwtTokenProvider.deleteRefreshToken(email);
        return ResponseEntity.ok(new ResponseDto<>("로그아웃 되었습니다.", null));
    }

    @Operation(summary = "swagger test", description = "swagger 테스트를 위한 엔드포인트 입니다.")
    @GetMapping("/test")
    public ResponseEntity<ResponseDto> test() {
        List<Integer> testList = List.of(1, 2, 3, 4, 5); //테스트
        return ResponseEntity.ok(new ResponseDto<>("테스트 리스트 반환 완료", testList));
    }
}
