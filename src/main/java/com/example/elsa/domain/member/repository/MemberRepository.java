package com.example.elsa.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.elsa.domain.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	Boolean existsByEmail(String email);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByMemberId(Long memberId);
}