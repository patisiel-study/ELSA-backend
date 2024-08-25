package com.example.elsa.domain.diagnosis.repository;

import com.example.elsa.domain.diagnosis.entity.Diagnosis;
import com.example.elsa.domain.diagnosis.entity.DiagnosisQnaSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosisQnaSetRepository extends JpaRepository<DiagnosisQnaSet, Long> {
    @Query("SELECT q.question FROM DiagnosisQnaSet q WHERE q.diagnosis.standardName = :standardName")
    List<String> findByDiagnosisStandardName(@Param("standardName") String standardName);

    List<DiagnosisQnaSet> findByDiagnosis(Diagnosis diagnosis);
}
