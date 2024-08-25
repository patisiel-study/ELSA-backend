package com.example.elsa.domain.diagnosis.service;

import com.example.elsa.domain.diagnosis.dto.*;
import com.example.elsa.domain.diagnosis.entity.Answer;
import com.example.elsa.domain.diagnosis.entity.Diagnosis;
import com.example.elsa.domain.diagnosis.entity.DiagnosisQnaSet;
import com.example.elsa.domain.diagnosis.entity.MemberDiagnosisList;
import com.example.elsa.domain.diagnosis.repository.DiagnosisQnaSetRepository;
import com.example.elsa.domain.diagnosis.repository.DiagnosisRepository;
import com.example.elsa.domain.diagnosis.repository.MemberDiagnosisListRepository;
import com.example.elsa.domain.member.entity.Member;
import com.example.elsa.domain.member.repository.MemberRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.ExcelHelper;
import com.example.elsa.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisQnaSetRepository diagnosisQnaSetRepository;
    private final MemberRepository memberRepository;
    private final MemberDiagnosisListRepository memberDiagnosisListRepository;

    public void createDiagnosisQuestions(MultipartFile file) {

        try {
            Map<String, List<String>> data = ExcelHelper.parseQnaFile(file);

            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                String standardName = entry.getKey();
                List<String> questions = entry.getValue();

                // Diagnosis 객체 생성 및 저장
                Diagnosis diagnosis = Diagnosis.createDiagnosis(standardName);
                diagnosisRepository.save(diagnosis);

                // 각 질문에 대해 DiagnosisQnaSet 객체 생성 및 저장
                for (String question : questions) {
                    DiagnosisQnaSet diagnosisQnaSet = DiagnosisQnaSet.builder()
                            .question(question)
                            .answer(Answer.NOT_ANSWERED)  // 빈 값으로 초기화
                            .diagnosis(diagnosis)
                            .build();
                    diagnosisQnaSetRepository.save(diagnosisQnaSet);
                }
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public List<StandardQuestionsDto> getDiagnosisQuestions() {
        List<Diagnosis> diagnosisList = diagnosisRepository.findAll();

        // Diagnosis 엔티티를 StandardQuestionsDto로 변환
        return diagnosisList.stream().map(diagnosis -> {
            List<StandardQuestionsDto.QuestionDto> questions = diagnosisQnaSetRepository.findByDiagnosis(diagnosis).stream()
                    .map(qnaSet -> new StandardQuestionsDto.QuestionDto(qnaSet.getId(), qnaSet.getQuestion()))
                    .collect(Collectors.toList());

            return StandardQuestionsDto.builder()
                    .standardName(diagnosis.getStandardName())
                    .questions(questions)
                    .build();
        }).collect(Collectors.toList());
    }

    public DiagnosisSubmitResponse submitDiagnosisResult(DiagnosisSubmitRequest request) {
        String email = SecurityUtil.getCurrentMemberEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 요청된 자가진단 답변 리스트를 처리하여 진단 결과 생성
        Map<Long, MemberDiagnosisList> memberDiagnosisMap = processAnswers(member, request.getAnswers());

        // 진단 결과에 대한 통계 계산
        Map<String, StandardScore> standardScores = calculateStandardScores(memberDiagnosisMap);
        Map<String, List<QuestionAnswerPair>> noOrNotApplicableMap = extractNoOrNotApplicableAnswers(memberDiagnosisMap);
        TotalScore totalScore = calculateTotalScore(memberDiagnosisMap);

        // 진단 결과를 DB에 저장
        memberDiagnosisListRepository.saveAll(memberDiagnosisMap.values());

        return new DiagnosisSubmitResponse(standardScores, noOrNotApplicableMap, totalScore);
    }

    private Map<Long, MemberDiagnosisList> processAnswers(Member member, List<AnswerDto> answers) {
        return answers.stream()
                .map(answerDto -> processSingleAnswer(member, answerDto))
                .distinct() // 중복 제거
                .collect(Collectors.toMap(
                        entry -> entry.getDiagnosis().getId(),
                        entry -> entry,
                        (existing, replacement) -> existing // 중복된 경우 기존 값을 사용
                ));
    }



    private MemberDiagnosisList processSingleAnswer(Member member, AnswerDto answerDto) {
        DiagnosisQnaSet qnaSet = getDiagnosisQnaSet(answerDto.getQuestionId());
        MemberDiagnosisList memberDiagnosis = getOrCreateMemberDiagnosisList(member, qnaSet.getDiagnosis());
        memberDiagnosis.updateStatistics(qnaSet.getQuestion(), answerDto.getAnswer());
        return memberDiagnosis;
    }

    private DiagnosisQnaSet getDiagnosisQnaSet(Long questionId) {
        return diagnosisQnaSetRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND));
    }

    private MemberDiagnosisList getOrCreateMemberDiagnosisList(Member member, Diagnosis diagnosis) {
        return memberDiagnosisListRepository.findByMemberAndDiagnosis(member, diagnosis)
                .orElseGet(() -> {
                    MemberDiagnosisList newList = MemberDiagnosisList.builder()
                            .member(member)
                            .diagnosis(diagnosis)
                            .totalCount(0)
                            .yesCount(0)
                            .noOrNotApplicableAnswers(new ArrayList<>())
                            .build();
                    memberDiagnosisListRepository.save(newList); // 새로운 리스트를 생성하고 즉시 저장
                    return newList;
                });
    }

    private Map<String, StandardScore> calculateStandardScores(Map<Long, MemberDiagnosisList> memberDiagnosisMap) {
        return memberDiagnosisMap.values().stream()
                .collect(Collectors.toMap(
                        list -> list.getDiagnosis().getStandardName(),
                        list -> {
                            int totalQuestions = list.getTotalCount();
                            int yesAnswers = list.getYesCount();
                            double ratio = totalQuestions > 0 ? (double) yesAnswers / totalQuestions : 0;
                            return new StandardScore(yesAnswers + "/" + totalQuestions, ratio);
                        }
                ));
    }


    private Map<String, List<QuestionAnswerPair>> extractNoOrNotApplicableAnswers(Map<Long, MemberDiagnosisList> memberDiagnosisMap) {
        return memberDiagnosisMap.values().stream()
                .filter(list -> !list.getNoOrNotApplicableAnswers().isEmpty())
                .collect(Collectors.toMap(
                        list -> list.getDiagnosis().getStandardName(),
                        MemberDiagnosisList::getNoOrNotApplicableAnswers
                ));
    }

    private TotalScore calculateTotalScore(Map<Long, MemberDiagnosisList> memberDiagnosisMap) {
        int totalYesCount = memberDiagnosisMap.values().stream()
                .mapToInt(MemberDiagnosisList::getYesCount).sum();

        int totalQuestions = memberDiagnosisMap.values().stream()
                .mapToInt(MemberDiagnosisList::getTotalCount).sum();

        double ratioDouble = totalQuestions > 0 ? (double) totalYesCount / totalQuestions : 0;
        String ratioString = totalYesCount + "/" + totalQuestions;

        return new TotalScore(ratioString, ratioDouble);
    }

}
