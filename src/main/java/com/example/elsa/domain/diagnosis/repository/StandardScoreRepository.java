package com.example.elsa.domain.diagnosis.repository;

import com.example.elsa.domain.diagnosis.entity.StandardScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandardScoreRepository extends JpaRepository<StandardScore, Long> {
    List<StandardScore> findByDiagnosisId(Long diagnosisId);
}
