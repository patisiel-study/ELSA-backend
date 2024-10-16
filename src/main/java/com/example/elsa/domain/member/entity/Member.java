package com.example.elsa.domain.member.entity;


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
    private String password;

    @Column(nullable = false)
    private Country country;

    @Column(nullable = false)
    private Career career;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(String email, String password, Role role, Country country, Career career) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.country = country;
        this.career = career;
    }

    @Builder
    public static Member createMember(SignUpRequest request, String encodedPassword, Role role) {
        return Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .role(role)
                .country(request.getCountry())
                .career(request.getCareer())
                .build();
    }
}
