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
import java.util.HashMap;
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

        // 각 Diagnosis별로 MemberDiagnosisList를 관리하기 위한 맵
        Map<Long, MemberDiagnosisList> memberDiagnosisMap = new HashMap<>();

        // 각 답변을 처리하여 Diagnosis별로 MemberDiagnosisList를 업데이트
        for (AnswerDto answerDto : request.getAnswers()) {
            DiagnosisQnaSet qnaSet = getDiagnosisQnaSet(answerDto.getQuestionId());
            Long diagnosisId = qnaSet.getDiagnosis().getId();

            // Diagnosis에 해당하는 MemberDiagnosisList가 없다면 새로 생성
            MemberDiagnosisList memberDiagnosis = memberDiagnosisMap.getOrDefault(diagnosisId,
                    MemberDiagnosisList.builder()
                            .member(member)
                            .diagnosis(qnaSet.getDiagnosis())
                            .totalCount(0)
                            .yesCount(0)
                            .noOrNotApplicableAnswers(new ArrayList<>())
                            .build());

            // 현재 진단 결과를 업데이트
            memberDiagnosis.updateStatistics(qnaSet.getQuestion(), answerDto.getAnswer());
            memberDiagnosisMap.put(diagnosisId, memberDiagnosis);
        }

        // 통계 계산
        Map<String, StandardScore> standardScores = calculateStandardScores(memberDiagnosisMap);
        Map<String, List<QuestionAnswerPair>> noOrNotApplicableMap = extractNoOrNotApplicableAnswers(memberDiagnosisMap);
        TotalScore totalScore = calculateTotalScore(memberDiagnosisMap);

        // 결과 저장
        memberDiagnosisListRepository.saveAll(memberDiagnosisMap.values());

        return new DiagnosisSubmitResponse(standardScores, noOrNotApplicableMap, totalScore);
    }


    public List<MemberDiagnosisListDto> getMemberDiagnosisHistory() {
        String email = SecurityUtil.getCurrentMemberEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MemberDiagnosisList> memberDiagnosisList = memberDiagnosisListRepository.findByMemberOrderByCreatedAtDesc(member);

        return memberDiagnosisList.stream()
                .map(diagnosisList -> new MemberDiagnosisListDto(
                        diagnosisList.getId(),
                        diagnosisList.getCreatedAt(),
                        diagnosisList.getTotalScoreString(),
                        diagnosisList.getTotalScoreDouble()
                ))
                .collect(Collectors.toList());
    }

    public DiagnosisSubmitResponse getSingleDiagnosisResult(Long diagnosisId) {
        String email = SecurityUtil.getCurrentMemberEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 해당 사용자의 특정 diagnosisId와 연결된 모든 MemberDiagnosisList를 가져옵니다.
        List<MemberDiagnosisList> memberDiagnosisLists = memberDiagnosisListRepository.findByMemberAndDiagnosisId(member, diagnosisId);

        if (memberDiagnosisLists.isEmpty()) {
            throw new CustomException(ErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND);
        }

        // standardScores를 생성합니다.
        Map<String, StandardScore> standardScores = new HashMap<>();
        Map<String, List<QuestionAnswerPair>> noOrNotApplicableMap = new HashMap<>();
        int totalYesCount = 0;
        int totalQuestions = 0;

        // 모든 MemberDiagnosisList 항목을 처리하여 결과를 통합합니다.
        for (MemberDiagnosisList memberDiagnosis : memberDiagnosisLists) {
            String standardName = memberDiagnosis.getDiagnosis().getStandardName();

            // StandardScore 계산
            StandardScore standardScore = new StandardScore(memberDiagnosis.getTotalScoreString(), memberDiagnosis.getTotalScoreDouble());
            standardScores.put(standardName, standardScore);

            // NO 또는 NOT_APPLICABLE 답변이 있는 경우 처리
            if (!memberDiagnosis.getNoOrNotApplicableAnswers().isEmpty()) {
                noOrNotApplicableMap.put(standardName, memberDiagnosis.getNoOrNotApplicableAnswers());
            }

            // 전체 총계에 추가
            totalYesCount += memberDiagnosis.getYesCount();
            totalQuestions += memberDiagnosis.getTotalCount();
        }

        // TotalScore 계산
        double totalRatio = totalQuestions > 0 ? (double) totalYesCount / totalQuestions : 0.0;
        TotalScore totalScore = new TotalScore(totalYesCount + "/" + totalQuestions, totalRatio);

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

    private Map<String, StandardScore> calculateStandardScoresForSingleDiagnosis(MemberDiagnosisList memberDiagnosis) {
        int totalQuestions = memberDiagnosis.getTotalCount();
        int yesAnswers = memberDiagnosis.getYesCount();
        double ratio = totalQuestions > 0 ? (double) yesAnswers / totalQuestions : 0;

        Map<String, StandardScore> standardScores = new HashMap<>();
        standardScores.put("Total", new StandardScore(yesAnswers + "/" + totalQuestions, ratio));

        return standardScores;
    }

    private Map<String, List<QuestionAnswerPair>> extractNoOrNotApplicableAnswersForSingleDiagnosis(MemberDiagnosisList memberDiagnosis) {
        if (!memberDiagnosis.getNoOrNotApplicableAnswers().isEmpty()) {
            return Map.of("Total", memberDiagnosis.getNoOrNotApplicableAnswers());
        }
        return new HashMap<>();
    }

    private TotalScore calculateTotalScoreForSingleDiagnosis(MemberDiagnosisList memberDiagnosis) {
        int totalYesCount = memberDiagnosis.getYesCount();
        int totalQuestions = memberDiagnosis.getTotalCount();
        double ratioDouble = totalQuestions > 0 ? (double) totalYesCount / totalQuestions : 0;
        String ratioString = totalYesCount + "/" + totalQuestions;

        return new TotalScore(ratioString, ratioDouble);
    }

}