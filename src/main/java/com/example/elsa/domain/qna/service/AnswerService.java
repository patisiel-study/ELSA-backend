package com.example.elsa.domain.qna.service;

import com.example.elsa.domain.qna.dto.ChatRequest;
import com.example.elsa.domain.qna.dto.ChatResponse;
import com.example.elsa.domain.qna.enums.LLMModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnswerService(@Qualifier("openaiRestTemplate") RestTemplate openaiRestTemplate,
                         @Qualifier("geminiRestTemplate") RestTemplate geminiRestTemplate) {
        this.openaiRestTemplate = openaiRestTemplate;
        this.geminiRestTemplate = geminiRestTemplate;
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswer(String question, LLMModel model) {
        switch (model) {
            case GPT_3_5:
                return getAnswerFromGPT3_5(question);
            case GPT_4:
                return getAnswerFromGPT4(question);
            case GPT_4o:
                return getAnswerFromGPT4o(question);
            case GEMINI:
                return getAnswerFromGemini(question);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
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

        try {
            ChatResponse response = openaiRestTemplate.postForObject(apiUrl, request, ChatResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.warn("No response from OpenAI API");
                return CompletableFuture.completedFuture("");
            }

            String answer = response.getChoices().get(0).getMessage().getContent().trim();
            log.info("GPT 응답: {}", answer);
            return CompletableFuture.completedFuture(answer);
        } catch (Exception e) {
            log.error("Error while calling OpenAI API: {}", e.getMessage());
            return CompletableFuture.completedFuture("");
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswerFromGemini(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        parts.add(Collections.singletonMap("text", question));
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            log.info("Sending request to Gemini API for question: {}", question);
            log.debug("Full Gemini API URL: {}", fullUrl);
            log.debug("Request body: {}", objectMapper.writeValueAsString(requestBody));

            ResponseEntity<String> response = geminiRestTemplate.postForEntity(fullUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                log.debug("Raw Gemini API response: {}", responseBody);

                String answer = extractAnswerFromResponse(responseBody);
                log.info("Extracted answer from Gemini: {}", answer);
                return CompletableFuture.completedFuture(answer);
            } else {
                log.error("Gemini API returned non-OK status: {}, Body: {}", response.getStatusCode(), response.getBody());
                return CompletableFuture.completedFuture("Error: Unable to get response from Gemini");
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: ", e);
            return CompletableFuture.completedFuture("Error: " + e.getMessage());
        }
    }

    private String extractAnswerFromResponse(String responseBody) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode candidatesNode = jsonNode.path("candidates");
        if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
            JsonNode firstCandidate = candidatesNode.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");
            if (partsNode.isArray() && !partsNode.isEmpty()) {
                return partsNode.get(0).path("text").asText();
            }
        }
        log.error("Unable to extract answer from response: {}", responseBody);
        return "Error: Unable to extract answer from response";
    }
}