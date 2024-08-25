package com.example.elsa.domain.diagnosis.service;

import com.example.elsa.domain.diagnosis.dto.StandardQuestionsDto;
import com.example.elsa.domain.diagnosis.entity.Answer;
import com.example.elsa.domain.diagnosis.entity.Diagnosis;
import com.example.elsa.domain.diagnosis.entity.DiagnosisQnaSet;
import com.example.elsa.domain.diagnosis.repository.DiagnosisQnaSetRepository;
import com.example.elsa.domain.diagnosis.repository.DiagnosisRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;
    private final DiagnosisQnaSetRepository diagnosisQnaSetRepository;

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
}
