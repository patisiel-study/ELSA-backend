package com.example.elsa.domain.dataset.repository;

import com.example.elsa.domain.dataset.entity.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataSetRepository extends JpaRepository<DataSet, Long> {
    Optional<DataSet> findByName(String name);
}