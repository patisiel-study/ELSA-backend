package com.example.elsa.domain.diagnosis.repository;

import com.example.elsa.domain.diagnosis.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    Optional<Diagnosis> findByMemberIdAndDiagnosisId(Long memberId, Long diagnosisId);

    List<Diagnosis> findByMemberIdOrderByTotalScoreDesc(Long memberId);
}
