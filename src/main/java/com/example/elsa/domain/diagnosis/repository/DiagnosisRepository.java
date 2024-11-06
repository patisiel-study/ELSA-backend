package com.example.elsa.domain.diagnosis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.elsa.domain.diagnosis.entity.Diagnosis;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
	Optional<Diagnosis> findByMemberIdAndDiagnosisId(Long memberId, Long diagnosisId);

	List<Diagnosis> findByMemberIdOrderByTotalScoreDesc(Long memberId);

	Optional<Diagnosis> findByDiagnosisId(Long diagnosisId);
}
