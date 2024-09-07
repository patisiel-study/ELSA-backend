package com.example.elsa.domain.diagnosis.service;

import com.example.elsa.domain.diagnosis.dto.DiagnosisSubmitRequest;
import com.example.elsa.domain.diagnosis.dto.QnaPairDto;
import com.example.elsa.domain.diagnosis.dto.StandardQuestionsDto;
import com.example.elsa.domain.diagnosis.entity.*;
import com.example.elsa.domain.diagnosis.repository.*;
import com.example.elsa.domain.member.entity.Member;
import com.example.elsa.domain.member.repository.MemberRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.ExcelHelper;
import com.example.elsa.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisQuestionRepository diagnosisQuestionRepository;
    private final MemberRepository memberRepository;
    private final MemberDiagnosisListRepository memberDiagnosisListRepository;
    private final DiagnosisQnaSetRepository diagnosisQnaSetRepository;
    private final StandardScoreRepository standardScoreRepository;
    private final NoOrNotApplicableRepository noOrNotApplicableRepository;

    public void createDiagnosisQuestions(MultipartFile file) {

        try {
            Map<String, List<String>> data = ExcelHelper.parseQnaFile(file);

            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                String standardName = entry.getKey();
                List<String> questions = entry.getValue();

                // 각 질문에 대해 DiagnosisQnaSet 객체 생성 및 저장
                questions.forEach(question -> {
                    DiagnosisQuestion diagnosisQuestion = DiagnosisQuestion.builder()
                            .question(question)
                            .standardName(standardName)
                            .build();
                    diagnosisQuestionRepository.save(diagnosisQuestion);
                });
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional(readOnly = true)
    public List<StandardQuestionsDto> getDiagnosisQuestionRepository() {
        List<DiagnosisQuestion> diagnosisQuestionList = diagnosisQuestionRepository.findAll();

        // diagnosisQuestionList를 standardName으로 그룹화하여 StandardQuestionsDto로 변환


        return diagnosisQuestionList.stream()
                .collect(Collectors.groupingBy(DiagnosisQuestion::getStandardName))
                .entrySet().stream()
                .map(entry -> new StandardQuestionsDto(entry.getKey(),
                        entry.getValue().stream()
                                .map(DiagnosisQuestion::toDto)  // toDto 메서드 사용
                                .toList()))
                .toList();

    }


    public void submitDiagnosisResult(DiagnosisSubmitRequest request) {
        // 진단 결과 생성 및 저장
        Long memberId = memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)).getMemberId();

        Diagnosis diagnosis = Diagnosis.createDiagnosis(memberId, 0.0);
        diagnosisRepository.save(diagnosis);  // 여기서 id가 생성됨

        Long diagnosisId = diagnosis.getId();

        // 각 standard별 YES, NO, NOT_APPLICABLE 답변 수 및 진단 점수 계산
        Map<String, Integer> yesCounts = new HashMap<>();
        Map<String, List<QnaPairDto>> noOrNotApplicableMap = new HashMap<>();

        request.getAnswers().forEach(answerDto -> {
                    log.debug("answer: {}", answerDto.getAnswer());
                    DiagnosisQuestion diagnosisQuestion = getDiagnosisQnaSet(answerDto.getQuestionId());
                    String question = diagnosisQuestion.getQuestion();
                    String standardName = diagnosisQuestion.getStandardName();

                    DiagnosisQnaSet diagnosisQnaSet = DiagnosisQnaSet.builder()
                            .question(question)
                            .answer(answerDto.getAnswer())
                            .standardName(standardName)
                            .diagnosisId(diagnosisId)
                            .build();

                    diagnosisQnaSetRepository.save(diagnosisQnaSet);

                    // Answer가 YES인 경우
                    if (answerDto.getAnswer() == Answer.YES) {
                        yesCounts.put(standardName, yesCounts.getOrDefault(standardName, 0) + 1);
                    }
                    // Answer가 NO 또는 미해당인 경우
                    else if (answerDto.getAnswer() == Answer.NO || answerDto.getAnswer() == Answer.NOT_APPLICABLE) {
                        noOrNotApplicableMap.putIfAbsent(standardName, new ArrayList<>());
                        noOrNotApplicableMap.get(standardName).add(new QnaPairDto(question, answerDto.getAnswer()));
                    }
                }
        );

        // 각 standard별로 점수 계산 및 저장
        yesCounts.forEach((standardName, yesCount) -> {
            StandardScore standardScore = StandardScore.builder()
                    .standardName(standardName)
                    .score(yesCount)
                    .diagnosisId(diagnosisId)
                    .build();
            standardScoreRepository.save(standardScore);
        });

        // NO 또는 미해당 답변 리스트 저장
        noOrNotApplicableMap.forEach((standardName, qnaPairDtoList) -> {
            NoOrNotApplicable noOrNotApplicable = NoOrNotApplicable.builder()
                    .standardName(standardName)
                    .qnaPairDtoList(qnaPairDtoList)
                    .diagnosisId(diagnosisId)
                    .build();
            noOrNotApplicableRepository.save(noOrNotApplicable);
        });

        // 총점 계산 (YES 답변 개수 기준으로 비율 계산)
        int totalYesAnswers = yesCounts.values().stream().mapToInt(Integer::intValue).sum();
        int totalQuestions = request.getAnswers().size();
        double ratioDouble = Math.round((totalYesAnswers / totalQuestions) * 100.0) / 100.0;
        String ratioString = totalYesAnswers + "/" + totalQuestions;

        // 계산된 점수를 Diagnosis 엔티티에 반영하고 저장
        double finalScore = ratioDouble * 100;
        diagnosis.updateTotalScore(finalScore);
        diagnosis.updateTotalScoreToString(ratioString);
        diagnosisRepository.save(diagnosis);  // 업데이트된 totalScore 저장
    }


//    public DiagnosisSubmitResponse submitDiagnosisResult2(DiagnosisSubmitRequest request) {
//        String email = SecurityUtil.getCurrentMemberEmail();
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
//
//        for (AnswerDto answerDto : request.getAnswers()) {
//            DiagnosisQuestions qnaSet = getDiagnosisQnaSet(answerDto.getQuestionId());
//            MemberDiagnosisList memberDiagnosis = getOrCreateMemberDiagnosisList(member, qnaSet.getDiagnosis());
//            memberDiagnosis.updateStatistics(qnaSet.getQuestion(), answerDto.getAnswer());
//        }
//
//    }

//    public DiagnosisSubmitResponse submitDiagnosisResult(DiagnosisSubmitRequest request) {
//        String email = SecurityUtil.getCurrentMemberEmail();
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
//
//        // 각 Diagnosis별로 MemberDiagnosisList를 관리하기 위한 맵
//        Map<Long, MemberDiagnosisList> memberDiagnosisMap = new HashMap<>();
//
//        // 각 답변을 처리하여 Diagnosis별로 MemberDiagnosisList를 업데이트
//        for (AnswerDto answerDto : request.getAnswers()) {
//            DiagnosisQuestions qnaSet = getDiagnosisQnaSet(answerDto.getQuestionId());
//            Long diagnosisId = qnaSet.getDiagnosis().getId();
//
//            // Diagnosis에 해당하는 MemberDiagnosisList가 없다면 새로 생성
//            MemberDiagnosisList memberDiagnosis = memberDiagnosisMap.getOrDefault(diagnosisId,
//                    MemberDiagnosisList.builder()
//                            .member(member)
//                            .diagnosis(qnaSet.getDiagnosis())
//                            .totalCount(0)
//                            .yesCount(0)
//                            .noOrNotApplicableAnswers(new ArrayList<>())
//                            .build());
//
//            // 현재 진단 결과를 업데이트
//            memberDiagnosis.updateStatistics(qnaSet.getQuestion(), answerDto.getAnswer());
//            memberDiagnosisMap.put(diagnosisId, memberDiagnosis);
//        }
//
//        // 통계 계산
//        Map<String, StandardScore> standardScores = calculateStandardScores(memberDiagnosisMap);
//        Map<String, List<QnaPairDto>> noOrNotApplicableMap = extractNoOrNotApplicableAnswers(memberDiagnosisMap);
//        TotalScoreDto totalScoreDto = calculateTotalScore(memberDiagnosisMap);
//
//        // 결과 저장
//        memberDiagnosisListRepository.saveAll(memberDiagnosisMap.values());
//
//        return new DiagnosisSubmitResponse(standardScores, noOrNotApplicableMap, totalScoreDto);
//    }


//    public List<MemberDiagnosisListDto> getMemberDiagnosisHistory() {
//        String email = SecurityUtil.getCurrentMemberEmail();
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
//
//        List<MemberDiagnosisList> memberDiagnosisList = memberDiagnosisListRepository.findByMemberOrderByCreatedAtDesc(member);
//
//        return memberDiagnosisList.stream()
//                .map(diagnosisList -> new MemberDiagnosisListDto(
//                        diagnosisList.getId(),
//                        diagnosisList.getCreatedAt(),
//                        diagnosisList.getTotalScoreString(),
//                        diagnosisList.getTotalScoreDouble()
//                ))
//                .collect(Collectors.toList());
//    }

//    public DiagnosisSubmitResponse getSingleDiagnosisResult(Long diagnosisId) {
//        String email = SecurityUtil.getCurrentMemberEmail();
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
//
//        // 해당 사용자의 특정 diagnosisId와 연결된 모든 MemberDiagnosisList를 가져옵니다.
//        List<MemberDiagnosisList> memberDiagnosisLists = memberDiagnosisListRepository.findByMemberAndDiagnosisId(member, diagnosisId);
//
//        if (memberDiagnosisLists.isEmpty()) {
//            throw new CustomException(ErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND);
//        }
//
//        // standardScores를 생성합니다.
//        Map<String, StandardScore> standardScores = new HashMap<>();
//        Map<String, List<QnaPairDto>> noOrNotApplicableMap = new HashMap<>();
//        int totalYesCount = 0;
//        int totalQuestions = 0;
//
//        // 모든 MemberDiagnosisList 항목을 처리하여 결과를 통합합니다.
//        for (MemberDiagnosisList memberDiagnosis : memberDiagnosisLists) {
//            String standardName = memberDiagnosis.getDiagnosis().getStandardName();
//
//            // StandardScore 계산
//            StandardScore standardScore = new StandardScore(memberDiagnosis.getTotalScoreString(), memberDiagnosis.getTotalScoreDouble());
//            standardScores.put(standardName, standardScore);
//
//            // NO 또는 NOT_APPLICABLE 답변이 있는 경우 처리
//            if (!memberDiagnosis.getNoOrNotApplicableAnswers().isEmpty()) {
//                noOrNotApplicableMap.put(standardName, memberDiagnosis.getNoOrNotApplicableAnswers());
//            }
//
//            // 전체 총계에 추가
//            totalYesCount += memberDiagnosis.getYesCount();
//            totalQuestions += memberDiagnosis.getTotalCount();
//        }
//
//        // TotalScore 계산
//        double totalRatio = totalQuestions > 0 ? (double) totalYesCount / totalQuestions : 0.0;
//        TotalScoreDto totalScoreDto = new TotalScoreDto(totalYesCount + "/" + totalQuestions, totalRatio);
//
//        return new DiagnosisSubmitResponse(standardScores, noOrNotApplicableMap, totalScoreDto);
//    }


//    private Map<Long, MemberDiagnosisList> processAnswers(Member member, List<AnswerDto> answers) {
//        return answers.stream()
//                .map(answerDto -> processSingleAnswer(member, answerDto))
//                .distinct() // 중복 제거
//                .collect(Collectors.toMap(
//                        entry -> entry.getDiagnosis().getId(),
//                        entry -> entry,
//                        (existing, replacement) -> existing // 중복된 경우 기존 값을 사용
//                ));
//    }



//    private MemberDiagnosisList processSingleAnswer(Member member, AnswerDto answerDto) {
//        DiagnosisQuestions qnaSet = getDiagnosisQnaSet(answerDto.getQuestionId());
//        MemberDiagnosisList memberDiagnosis = getOrCreateMemberDiagnosisList(member, qnaSet.getDiagnosis());
//        memberDiagnosis.updateStatistics(qnaSet.getQuestion(), answerDto.getAnswer());
//        return memberDiagnosis;
//    }

    private DiagnosisQuestion getDiagnosisQnaSet(Long questionId) {
        return diagnosisQuestionRepository.findById(questionId)
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

}