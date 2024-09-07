package com.example.elsa.domain.diagnosis.repository;

import com.example.elsa.domain.diagnosis.entity.NoOrNotApplicable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoOrNotApplicableRepository extends JpaRepository<NoOrNotApplicable, Long> {
}
