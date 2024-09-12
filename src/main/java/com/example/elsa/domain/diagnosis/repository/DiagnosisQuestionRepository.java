package com.example.elsa.domain.diagnosis.repository;

import com.example.elsa.domain.diagnosis.entity.DiagnosisQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosisQuestionRepository extends JpaRepository<DiagnosisQuestion, Long> {
    @Query("SELECT q.question FROM DiagnosisQuestion q WHERE q.standardName = :standardName")
    List<String> findByDiagnosisStandardName(@Param("standardName") String standardName);
}
