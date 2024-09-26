package com.example.elsa.domain.qna.repository;

import com.example.elsa.domain.qna.entity.Standard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StandardRepository extends JpaRepository<Standard, Long> {
    Optional<Standard> findByName(String name);
    List<Standard> findByNameIn(List<String> names);

    @Query("SELECT s FROM Standard s WHERE s.name IS NOT NULL AND TRIM(s.name) <> ''")
    List<Standard> findAllValidStandards();
}
