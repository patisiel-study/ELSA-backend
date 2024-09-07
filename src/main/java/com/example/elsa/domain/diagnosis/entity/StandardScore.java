package com.example.elsa.domain.diagnosis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String standardName;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Long diagnosisId;
}