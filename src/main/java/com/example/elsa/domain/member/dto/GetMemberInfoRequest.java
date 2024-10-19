package com.example.elsa.domain.member.dto;

import com.example.elsa.domain.member.entity.Member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetMemberInfoRequest {
	private String email;
	private String name;
	private String role;
	private String country;
	private String career;
	private boolean nonMember;

	@Builder
	public GetMemberInfoRequest(String email, String name, String role, String country, String career,
		boolean nonMember) {
		this.email = email;
		this.name = name;
		this.role = role;
		this.country = country;
		this.career = career;
		this.nonMember = nonMember;
	}

	public static GetMemberInfoRequest from(Member member) {
		return GetMemberInfoRequest.builder()
			.email(member.getEmail())
			.role(member.getRole().name())
			.country(member.getCountry().name())
			.career(member.getCareer().name())
			.name(member.getName())
			.nonMember(member.isNonMember())
			.build();
	}
}

