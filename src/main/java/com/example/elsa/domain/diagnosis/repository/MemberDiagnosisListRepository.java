package com.example.elsa.domain.diagnosis.repository;

import com.example.elsa.domain.diagnosis.entity.Diagnosis;
import com.example.elsa.domain.diagnosis.entity.MemberDiagnosisList;
import com.example.elsa.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberDiagnosisListRepository extends JpaRepository<MemberDiagnosisList, Long> {
    Optional<MemberDiagnosisList> findByMemberAndDiagnosis(Member member, Diagnosis diagnosis);
}
