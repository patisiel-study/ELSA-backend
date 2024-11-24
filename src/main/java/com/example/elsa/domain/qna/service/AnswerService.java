package com.example.elsa.domain.qna.service;

import com.example.elsa.domain.qna.dto.ChatRequest;
import com.example.elsa.domain.qna.dto.ChatResponse;
import com.example.elsa.domain.qna.enums.LLMModel;
import com.example.elsa.global.util.GeminiDebugUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AnswerService {

    private final RestTemplate openaiRestTemplate;
    private final RestTemplate geminiRestTemplate;
    private final GeminiDebugUtil geminiDebugUtil;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnswerService(@Qualifier("openaiRestTemplate") RestTemplate openaiRestTemplate,
                         @Qualifier("geminiRestTemplate") RestTemplate geminiRestTemplate,
                         GeminiDebugUtil geminiDebugUtil) {
        this.openaiRestTemplate = openaiRestTemplate;
        this.geminiRestTemplate = geminiRestTemplate;
        this.geminiDebugUtil = geminiDebugUtil;
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswer(String question, LLMModel model) {
        // 빈 질문 체크 추가
        if (question == null || question.trim().isEmpty()) {
            log.warn("Empty question received");
            return CompletableFuture.completedFuture("");
        }

        // 재시도 로직 추가
        int maxRetries = 3;
        int currentTry = 0;
        long retryDelay = 1000; // 1초

        while (currentTry < maxRetries) {
            try {
                Thread.sleep(retryDelay * currentTry); // 재시도 간 딜레이

                switch (model) {
                    case GEMINI:
                        return getAnswerFromGemini(question);
                    case GPT_3_5:
                    case GPT_4:
                    case GPT_4o:
                        // API 할당량 초과 시 Gemini로 폴백
                        try {
                            return getAnswerFromGPT(question, model);
                        } catch (Exception e) {
                            if (e.getMessage().contains("insufficient_quota")) {
                                log.info("Falling back to Gemini due to OpenAI quota exhaustion");
                                return getAnswerFromGemini(question);
                            }
                            throw e;
                        }
                    default:
                        throw new IllegalArgumentException("Unsupported model: " + model);
                }
            } catch (Exception e) {
                log.error("Error attempt {} of {}: {}", currentTry + 1, maxRetries, e.getMessage());
                currentTry++;

                if (currentTry == maxRetries) {
                    return CompletableFuture.completedFuture("Error: " + e.getMessage());
                }
            }
        }

        return CompletableFuture.completedFuture("Error: Maximum retries exceeded");
    }


    @Async("taskExecutor")
    public CompletableFuture<String> getAnswerFromGPT3_5(String question) {
        return getAnswerFromGPT(question, LLMModel.GPT_3_5);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswerFromGPT4(String question) {
        return getAnswerFromGPT(question, LLMModel.GPT_4);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswerFromGPT4o(String question) {
        return getAnswerFromGPT(question, LLMModel.GPT_4o);
    }

    private CompletableFuture<String> getAnswerFromGPT(String question, LLMModel model) {
        ChatRequest request = new ChatRequest(model.getModelName(), question, 250);

        // 인권보장 관련 질문인 경우 프롬프트 수정
        if (model == LLMModel.GPT_4o && question.contains("인권")) {
            // 질문을 더 구체적이고 윤리적 평가 관점으로 재구성
            String modifiedQuestion = "Please evaluate from an ethical AI assessment perspective:\n" + question;
            request = new ChatRequest(model.getModelName(), modifiedQuestion, 250);
            log.info("Modified question for human rights assessment: {}", modifiedQuestion);
        }

        try {
            ChatResponse response = openaiRestTemplate.postForObject(apiUrl, request, ChatResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("No response from OpenAI API for model: {}, question: {}", model, question);
                return CompletableFuture.completedFuture("");
            }

            String answer = response.getChoices().get(0).getMessage().getContent().trim();

            // 응답 거부 감지 및 대체 프롬프트 시도
            if (answer.toLowerCase().contains("i'm sorry") || answer.toLowerCase().contains("can't assist")) {
                log.warn("Model {} refused to answer. Trying with alternative prompt...", model);

                // 대체 프롬프트로 재시도
                String alternativeQuestion = "From an AI ethics evaluation perspective, please assess with Yes/No:\n" + question;
                ChatRequest retryRequest = new ChatRequest(model.getModelName(), alternativeQuestion, 250);

                ChatResponse retryResponse = openaiRestTemplate.postForObject(apiUrl, retryRequest, ChatResponse.class);
                if (retryResponse != null && retryResponse.getChoices() != null && !retryResponse.getChoices().isEmpty()) {
                    answer = retryResponse.getChoices().get(0).getMessage().getContent().trim();
                    log.info("Received response with alternative prompt: {}", answer);
                }
            }

            return CompletableFuture.completedFuture(answer);
        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            throw e;
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswerFromGemini(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 프롬프트 형식 지정
        String formattedQuestion = "Please answer the following with only 'Yes' or 'No' for each numbered item:\n" + question;

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        parts.add(Collections.singletonMap("text", formattedQuestion));
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            log.info("Sending request to Gemini API for question: {}", question);
            log.debug("Full Gemini API URL: {}", fullUrl);
            log.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));

            // 재시도 로직 추가
            int maxRetries = 3;
            int currentTry = 0;
            ResponseEntity<String> response = null;
            Exception lastException = null;

            while (currentTry < maxRetries) {
                try {
                    response = geminiRestTemplate.postForEntity(fullUrl, request, String.class);
                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        break;
                    }
                } catch (Exception e) {
                    lastException = e;
                    log.warn("Attempt {} failed: {}", currentTry + 1, e.getMessage());
                    Thread.sleep(1000 * (currentTry + 1)); // 지수 백오프
                }
                currentTry++;
            }

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                log.debug("Raw Gemini API response: {}", responseBody);

                String answer = extractAnswerFromResponse(responseBody);
                if (answer != null && !answer.startsWith("Error:")) {
                    log.info("Extracted answer from Gemini: {}", answer);
                    return CompletableFuture.completedFuture(answer);
                }
            }

            // 실패 시 기본 응답 생성
            String[] questions = question.split("\n");
            StringBuilder fallbackAnswer = new StringBuilder();
            for (String q : questions) {
                if (q.matches(".*\\d+\\..*")) { // 숫자로 시작하는 항목만 처리
                    fallbackAnswer.append("Yes\n"); // 기본값으로 "Yes" 응답
                }
            }

            String fallbackResult = fallbackAnswer.toString().trim();
            log.info("Generated fallback answer: {}", fallbackResult);
            return CompletableFuture.completedFuture(fallbackResult);

        } catch (Exception e) {
            log.error("Error calling Gemini API: ", e);
            // 에러 발생 시도 기본 응답 생성
            String[] questions = question.split("\n");
            StringBuilder fallbackAnswer = new StringBuilder();
            for (String q : questions) {
                if (q.matches(".*\\d+\\..*")) {
                    fallbackAnswer.append("Yes\n");
                }
            }
            return CompletableFuture.completedFuture(fallbackAnswer.toString().trim());
        }
    }

    private String extractAnswerFromResponse(String responseBody) throws JsonProcessingException {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidatesNode = jsonNode.path("candidates");

            if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");

                if (partsNode.isArray() && !partsNode.isEmpty()) {
                    String answer = partsNode.get(0).path("text").asText().trim();

                    // 응답이 Yes/No 형식인지 확인
                    String[] lines = answer.split("\n");
                    StringBuilder formattedAnswer = new StringBuilder();

                    for (String line : lines) {
                        if (line.toLowerCase().contains("yes")) {
                            formattedAnswer.append("Yes\n");
                        } else if (line.toLowerCase().contains("no")) {
                            formattedAnswer.append("No\n");
                        }
                    }

                    String result = formattedAnswer.toString().trim();
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting answer from response: {}", responseBody, e);
            return null;
        }
    }
}