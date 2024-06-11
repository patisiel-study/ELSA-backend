package com.example.elsa.domain.qna.service;

import com.example.elsa.domain.dataset.entity.DataSet;
import com.example.elsa.domain.dataset.repository.DataSetRepository;
import com.example.elsa.domain.qna.dto.ChatRequest;
import com.example.elsa.domain.qna.dto.ChatResponse;
import com.example.elsa.domain.qna.dto.QnaToStandardDto;
import com.example.elsa.domain.qna.dto.StandardDto;
import com.example.elsa.domain.qna.entity.QnaSet;
import com.example.elsa.domain.qna.entity.Standard;
import com.example.elsa.domain.qna.repository.QnaSetRepository;
import com.example.elsa.domain.qna.repository.StandardRepository;
import com.example.elsa.global.error.CustomException;
import com.example.elsa.global.error.ErrorCode;
import com.example.elsa.global.util.DataFormatting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;
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

    @Qualifier("openaiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;


    @Value("${openai.api.url}")
    private String apiUrl;

    public void addInitialStandards(List<String> standardNames) {
        for (String name: standardNames) {
            if (standardRepository.findByName(name).isEmpty()) {
                createNewStandard(name);
            }
        }
    }

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

    public void addQnaToStandard(QnaToStandardDto qnaToStandardDto) {
        // 데이터가 있으면 하위 항목 추가, 없으면 에러 발생.
        List<String> standardNameList = qnaToStandardDto.getStandardNameList();
        String question = qnaToStandardDto.getQuestion().trim();

        // 해당 Standard 객체를 불러오기
        List<Standard> standards = standardRepository.findByNameIn(standardNameList);
        if (standards.isEmpty()) {
            throw new CustomException(ErrorCode.DATA_NOT_FOUND);
        }

        // question에 '{}'로 감싸진 단어가 있다면, 해당 단어에 대해 Data_set 엔티티에서 랜덤으로 keyword 값을 가져오고,
        // 기존의 '{}'형태의 값을 keyword 값으로 변환한다.
        String modifiedQuestion = replaceKeywordsInQuestion(question);

        // GPT 모델에 modifiedQuestion에 대한 응답을 받아오는 기능
        String answer = getAnswerFromGPT(modifiedQuestion);

        // 미리 QnaSet 객체 생성
        QnaSet qnaSet = new QnaSet(modifiedQuestion, answer);

        // 찾은 Standard 엔티티들에 QnaSet 추가
        for (Standard standard : standards) {
            standard.addQnaSet(qnaSet);
        }

        // 변경된 Standard 엔티티들을 DB에 저장
        standardRepository.saveAll(standards);

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

    // quetion에서 {}로 감싸진 모든 단어들에 대해 랜덤 키워드 변환 작업 수행
    private String replaceKeywordsInQuestion(String question) {
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(question);
        String modifiedQuestion = question;

        while (matcher.find()) {
            String keyword = matcher.group(1);
            String replacement = getRandomKeywordForDataSet(keyword);

            // 매칭된 부분만 대체하여 새로운 문장 생성
            modifiedQuestion = modifiedQuestion.replace(matcher.group(), replacement);

            // 변경된 question에 대해 다시 matcher를 생성하여 루프 진행
            matcher = Pattern.compile("\\{(.*?)\\}").matcher(modifiedQuestion);
        }

        log.info("변경된 질문: {}", modifiedQuestion);
        return modifiedQuestion;
    }

    // 해당 데이터 셋의 하위 목록중 랜덤으로 키워드 하나 선택
    private String getRandomKeywordForDataSet(String dataSetName) {
        return dataSetRepository.findByName(dataSetName)
                .map(DataSet::getKeywords)
                .filter(keywords -> !keywords.isEmpty())
                .map(keywords -> keywords.get(new Random().nextInt(keywords.size())))
                .orElse(dataSetName);
    }

    // GPT 모델에 modifiedQuestion에 대한 응답을 받아오는 기능
    private String getAnswerFromGPT(String question) {
        // Create a request
        ChatRequest request = new ChatRequest("gpt-3.5-turbo", question);

        try {
            // Call the OpenAI API
            ChatResponse response = restTemplate.postForObject(apiUrl, request, ChatResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.warn("No response from OpenAI API");
                return "";
            }

            // Extract and return the assistant's reply from the first choice
            String answer = response.getChoices().get(0).getMessage().getContent().trim();
            log.info("GPT 응답: {}", answer);
            return answer;
        } catch (Exception e) {
            log.error("Error while calling OpenAI API: {}", e.getMessage());
            return "";
        }
    }

}
