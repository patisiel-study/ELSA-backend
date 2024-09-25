package com.example.elsa.domain.qna.repository;

import com.example.elsa.domain.qna.entity.ModelScore;
import com.example.elsa.domain.qna.enums.LLMModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelScoreRepository extends JpaRepository<ModelScore, Long> {
    Optional<ModelScore> findByStandardNameAndModel(String standardName, LLMModel model);
}