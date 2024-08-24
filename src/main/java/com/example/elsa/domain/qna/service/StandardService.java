package com.example.elsa.domain.qna.service;

import com.example.elsa.domain.dataset.entity.DataSet;
import com.example.elsa.domain.dataset.repository.DataSetRepository;
import com.example.elsa.domain.qna.dto.QnaToStandardDto;
import com.example.elsa.domain.qna.dto.StandardDto;
import com.example.elsa.domain.qna.entity.QnaSet;
import com.example.elsa.domain.qna.entity.Standard;
import com.example.elsa.domain.qna.repository.QnaSetRepository;
import com.example.elsa.domain.qna.repository.StandardRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.DataFormatting;
import com.example.elsa.global.util.ExcelHelper;
import com.example.elsa.global.util.PythonExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StandardService {
    private final StandardRepository standardRepository;
    private final DataSetRepository dataSetRepository;
    private final QnaSetRepository qnaSetRepository;
    private final AnswerService answerService;
    private final PythonExecutor pythonExecutor;

    @Value("${openai.api.url}")

    public Map<String, Double> analyzeAllStandardQnaSentiments() {
        long startTime = System.currentTimeMillis();

        List<Standard> standards = standardRepository.findAll();

        Map<String, Double> result = standards.stream().collect(Collectors.toMap(
                Standard::getName,
                standard -> {
                    AtomicInteger initialScore = new AtomicInteger(standard.getQnaSetList().size());
                    AtomicInteger adjustedScore = new AtomicInteger(initialScore.get());

                    List<CompletableFuture<Void>> futures = standard.getQnaSetList().stream()
                            .map(qnaSet -> pythonExecutor.executeSentimentAnalysis(qnaSet.getAnswer())
                                    .thenAccept(analysisResult -> {
                                        if (analysisResult == null || analysisResult.get("average_compound_score") == null || (double)analysisResult.get("average_compound_score") == -2.0) {
                                            initialScore.decrementAndGet();
                                            adjustedScore.decrementAndGet();
                                            log.info("Standard {} has a QnA with invalid sentiment. Adjusted score: {}", standard.getName(), adjustedScore.get());
                                        }
                                        else {
                                            double averageScore = (double) analysisResult.get("average_compound_score");

                                            if (averageScore > 0) {
                                                adjustedScore.decrementAndGet();
                                                log.info("Standard {} has negative sentiment for QnA. Adjusted score: {}", standard.getName(), adjustedScore.get());
                                            }

                                            // QnaSet에 감성 분석 결과를 저장
                                            qnaSet.setSentimentScore(averageScore);
                                            qnaSetRepository.save(qnaSet);
                                        }
                                    }))
                            .collect(Collectors.toList());

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    double finalScore = (double) adjustedScore.get() / initialScore.get();
                    log.info("Standard {} final score: {}", standard.getName(), finalScore);
                    return finalScore;
                }
        ));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Execution time for analyzeAllStandardQnaSentiments: {} ms", duration);

        return result;
    }

    public CompletableFuture<Void> processQnaAsync(String standardName, List<String> questions) {
        Standard standard = standardRepository.findByName(standardName)
                .orElseGet(() -> new Standard(standardName));

        List<CompletableFuture<QnaSet>> futureQnaSets = questions.stream()
                .map(question -> {
                    String modifiedQuestion = replaceKeywordsInQuestion(question);
                    return answerService.getAnswerFromGPT(modifiedQuestion)
                            .thenApply(answer -> new QnaSet(modifiedQuestion, answer));
                })
                .collect(Collectors.toList());

        CompletableFuture.allOf(futureQnaSets.toArray(new CompletableFuture[0])).join();

        List<QnaSet> qnaSets = futureQnaSets.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        qnaSets.forEach(standard::addQnaSet);
        standardRepository.save(standard);
        return CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> addQnaToStandard(QnaToStandardDto qnaToStandardDto) {
        List<String> standardNameList = qnaToStandardDto.getStandardNameList();
        String question = qnaToStandardDto.getQuestion().trim();

        List<Standard> standards = standardRepository.findByNameIn(standardNameList);
        if (standards.isEmpty()) {
            throw new CustomException(ErrorCode.DATA_NOT_FOUND);
        }

        String modifiedQuestion = replaceKeywordsInQuestion(question);
        CompletableFuture<String> answerFuture = answerService.getAnswerFromGPT(modifiedQuestion);

        return answerFuture.thenAccept(answer -> {
            QnaSet qnaSet = new QnaSet(modifiedQuestion, answer);
            for (Standard standard : standards) {
                standard.addQnaSet(qnaSet);
            }
            standardRepository.saveAll(standards);
        });
    }


    public void uploadAndProcessQna(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            Map<String, List<String>> data = ExcelHelper.parseQnaFile(file);
            List<CompletableFuture<Void>> futures = data.entrySet().stream()
                    .map(entry -> processQnaAsync(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("uploadAndProcessQna 메서드 실행 시간: {} ms", duration);
        }
    }

//    public void addInitialStandards(List<String> standardNames) {
//        for (String name: standardNames) {
//            if (standardRepository.findByName(name).isEmpty()) {
//                createNewStandard(name);
//            }
//        }
//    }

    public void addStandard(StandardDto standardDto) {

        String standardName = standardDto.getName().trim();

        if (DataFormatting.isNullOrEmpty(standardName)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_BODY);
        }

        // 해당 이름의 데이터 셋 있는지 확인
        standardRepository.findByName(standardName)
                // 이미 있으면 에러 발생, 없으면 새로 만들기
                .ifPresentOrElse(
                        dataSet -> {
                            throw new CustomException(ErrorCode.DUPLICATE_DATA);
                        },
                        () -> createNewStandard(standardName)

                );
    }

    public List<String> getAllStandardNames() {
        return standardRepository.findAll()
                .stream()
                .map(Standard::getName)
                .collect(Collectors.toList());
    }

    public List<QnaSet> getAllQnaByStandardName(String standardName) {
        return standardRepository.findByName(standardName)
                .map(Standard::getQnaSetList)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
    }

    public void removeQnaFromStandard(String standardName, Long qnaSetId) {
        Standard standard = standardRepository.findByName(standardName)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
        QnaSet qnaSet = standard.getQnaSetList().stream()
                .filter(q -> q.getId().equals(qnaSetId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
        standard.removeQnaSet(qnaSet);
        standardRepository.save(standard);
        qnaSetRepository.delete(qnaSet);
    }

    private void createNewStandard(String standardName) {
        Standard standard = new Standard(standardName);
        try {
            standardRepository.save(standard);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // question에서 {}로 감싸진 모든 단어들에 대해 랜덤 키워드 변환 작업 수행
    private String replaceKeywordsInQuestion(String question) {
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(question);
        StringBuffer modifiedQuestion = new StringBuffer();

        while (matcher.find()) {
            String keyword = matcher.group(1).toLowerCase(); // {} 안의 문자를 소문자로 변경
            String replacement = getRandomKeywordForDataSet(keyword);
            log.info("기존: {} -> 변경: {}", keyword, replacement);

            // 매칭된 부분을 대체하여 새로운 문장 생성
            matcher.appendReplacement(modifiedQuestion, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(modifiedQuestion);

        log.info("변경된 질문: {}", modifiedQuestion);
        return modifiedQuestion.toString();
    }

    // 해당 데이터 셋의 하위 목록중 랜덤으로 키워드 하나 선택
    private String getRandomKeywordForDataSet(String dataSetName) {

        return dataSetRepository.findByName(dataSetName)
                .map(DataSet::getKeywords)
                .filter(keywords -> !keywords.isEmpty())
                .map(keywords -> keywords.get(new Random().nextInt(keywords.size())))
                .orElse(dataSetName);
    }

}
