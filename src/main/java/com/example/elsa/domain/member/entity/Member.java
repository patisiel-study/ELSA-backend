package com.example.elsa.domain.member.entity;


import com.example.elsa.domain.diagnosis.dto.NonMemberDiagnosisSubmitRequest;
import com.example.elsa.domain.member.dto.SignUpRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean nonMember;

    @Column(nullable = false)
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Career career;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(String email, String password, Role role, Country country, Career career, String name, boolean nonMember) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.country = country;
        this.career = career;
        this.name = name;
        this.nonMember = nonMember;
    }

    @Builder
    public static Member createMember(SignUpRequest request, String encodedPassword, Role role) {
        return Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .role(role)
                .country(request.getCountry())
                .career(request.getCareer())
                .name(request.getName())
                .nonMember(false)
                .build();
    }

    public static Member createNonMember(NonMemberDiagnosisSubmitRequest request) {
        return Member.builder()
                .email("nonMember")
                .role(Role.NON_MEMBER)
                .password("nonMember")
                .country(request.getCountry())
                .career(request.getCareer())
                .name("비회원")
                .nonMember(true)
                .build();
    }
}
