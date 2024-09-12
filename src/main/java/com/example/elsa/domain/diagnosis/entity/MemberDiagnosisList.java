package com.example.elsa.domain.diagnosis.entity;

import com.example.elsa.domain.diagnosis.dto.QnaPairDto;
import com.example.elsa.domain.member.entity.Member;
import com.example.elsa.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDiagnosisList extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_diagnosis_list_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(nullable = false)
    private Integer totalCount = 0;

    @Column(nullable = false)
    private int yesCount = 0;

    @ElementCollection
    @CollectionTable(name = "no_or_not_applicable_answers", joinColumns = @JoinColumn(name = "member_diagnosis_list_id"))
    @Column(name = "question_answer_pair")
    private List<QnaPairDto> noOrNotApplicableAnswers = new ArrayList<>();

    public void updateStatistics(String question, Answer answer) {
        boolean alreadyExists = noOrNotApplicableAnswers.stream()
                .anyMatch(pair -> pair.getQuestion().equals(question) && pair.getAnswer().equals(answer));

        if (!alreadyExists) {
            totalCount++;
            if (answer == Answer.YES) {
                yesCount++;
            } else if (answer == Answer.NO || answer == Answer.NOT_APPLICABLE) {
                noOrNotApplicableAnswers.add(new QnaPairDto(question, answer));
            }
        }
    }

    public void assignMember(Member member) {
        this.member = member;
    }

    public String getRatioString() {
        return yesCount + "/" + totalCount;
    }

    public double getRatioDouble() {
        return (double) yesCount / totalCount;
    }

    public void merge(MemberDiagnosisList other) {
        this.totalCount += other.getTotalCount();
        this.yesCount += other.getYesCount();
        this.noOrNotApplicableAnswers.addAll(other.getNoOrNotApplicableAnswers());
    }

    public String getTotalScoreString() {
        return yesCount + "/" + totalCount;
    }

    public double getTotalScoreDouble() {
        return totalCount > 0 ? (double) yesCount / totalCount : 0.0;
    }
}
