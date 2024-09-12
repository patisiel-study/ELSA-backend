package com.example.elsa.domain.diagnosis.entity;

import com.example.elsa.domain.diagnosis.dto.QnaPairDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoOrNotApplicable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String standardName;

    @ElementCollection
    private List<QnaPairDto> qnaPairDtoList;

    @Column(nullable = false)
    private Long diagnosisId;

}