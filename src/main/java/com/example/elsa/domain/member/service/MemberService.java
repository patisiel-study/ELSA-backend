package com.example.elsa.domain.member.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.elsa.domain.member.dto.GetMemberInfoRequest;
import com.example.elsa.domain.member.dto.SignUpRequest;
import com.example.elsa.domain.member.entity.Career;
import com.example.elsa.domain.member.entity.Country;
import com.example.elsa.domain.member.entity.Member;
import com.example.elsa.domain.member.repository.MemberRepository;
import com.example.elsa.global.auth.JwtToken;
import com.example.elsa.global.auth.JwtTokenProvider;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	public void signUp(SignUpRequest request) {
		if (memberRepository.existsByEmail(request.getEmail())) {
			throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
		}
		// 넘겨받은 비밀번호를 인코딩하여 DB에 저장한다.
		String encodedPassword = passwordEncoder.encode(request.getPassword());
		// 정적 팩토리 메서드를 사용하여 Member 객체 생성
		Member member = Member.createMember(request, encodedPassword, request.getRole());
		memberRepository.save(member);
	}

	public JwtToken logIn(String studentId, String password) {
		// 1. username + password 기반으로 Authentication 객체 생성
		// 로그인 요청시에는 아직 미인증 상태이므로 authentication은 인증 여부를 확인하는 authenticated 값이 false 상태이다.

		try {
			// 1. username + password 기반으로 Authentication 객체 생성
			// 2. AuthenticationManager를 통한 인증 요청
			Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(studentId, password)
			);
			// 인증된 Authentication 객체 봔환
			// 인증 상태이므로 authentication은 인증 여부를 확인하는 authenticated 값이 true 상태이다.
			return jwtTokenProvider.generateToken(authentication);
		} catch (AuthenticationException e) {
			throw new CustomException(ErrorCode.LOGIN_FAILURE);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	public GetMemberInfoRequest getMemberInfo(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		return GetMemberInfoRequest.from(member);
	}

	public List<String> getAllCountries() {
		return Arrays.stream(Country.values())
			.map(Country::getDescription)
			.collect(Collectors.toList());
	}

	public List<String> getAllCareers() {
		return Arrays.stream(Career.values())
			.map(Career::getDescription)
			.collect(Collectors.toList());
	}
}
