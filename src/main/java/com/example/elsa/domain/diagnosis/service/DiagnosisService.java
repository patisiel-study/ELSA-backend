package com.example.elsa.domain.diagnosis.service;

import com.example.elsa.domain.diagnosis.dto.*;
import com.example.elsa.domain.diagnosis.entity.StandardScore;
import com.example.elsa.domain.diagnosis.entity.*;
import com.example.elsa.domain.diagnosis.repository.*;
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

        Long diagnosisId = diagnosis.getDiagnosisId();

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
        double ratioDouble = Math.round(((double) totalYesAnswers / totalQuestions) * 100.0) / 100.0;
        String ratioString = totalYesAnswers + "/" + totalQuestions;

        // 계산된 점수를 Diagnosis 엔티티에 반영하고 저장
        double finalScore = ratioDouble * 100;
        diagnosis.updateTotalScore(finalScore);
        diagnosis.updateTotalScoreToString(ratioString);
        diagnosisRepository.save(diagnosis);  // 업데이트된 totalScore 저장
    }

    @Transactional(readOnly = true)
    public DiagnosisSubmitResponse getDiagnosisDetails(Long diagnosisId) {
        Long memberId = memberRepository.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)).getMemberId();

        Diagnosis diagnosis = diagnosisRepository.findByMemberIdAndDiagnosisId(memberId, diagnosisId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        List<StandardScoreDto> standardScoreDtoList = standardScoreRepository.findByDiagnosisId(diagnosisId)
                .stream()
                .map(req -> new StandardScoreDto(req.getStandardName(), req.getScore()))
                .toList();

        List<NoOrNotApplicableDto> noOrNotApplicableDtoList = noOrNotApplicableRepository.findByDiagnosisId(diagnosisId)
                .stream()
                .map(req -> new NoOrNotApplicableDto(
                        req.getStandardName(),
                        req.getQnaPairDtoList()))
                .toList();

        String totalScoreString = diagnosis.getTotalScoreToString();
        double totalScore = diagnosis.getTotalScore();

        return new DiagnosisSubmitResponse(
                standardScoreDtoList,
                noOrNotApplicableDtoList,
                new TotalScoreDto(totalScoreString, totalScore)
        );
    }

    private DiagnosisQuestion getDiagnosisQnaSet(Long questionId) {
        return diagnosisQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIAGNOSIS_QUESTION_NOT_FOUND));
    }
}