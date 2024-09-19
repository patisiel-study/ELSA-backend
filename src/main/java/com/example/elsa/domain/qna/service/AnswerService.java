package com.example.elsa.domain.qna.service;

import com.example.elsa.domain.qna.dto.ChatRequest;
import com.example.elsa.domain.qna.dto.ChatResponse;
import com.example.elsa.domain.qna.enums.LLMModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    @Qualifier("openaiRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
            ChatResponse response = restTemplate.postForObject(apiUrl, request, ChatResponse.class);

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

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));
//
//        String requestBody = String.format(
//                "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"temperature\": 0.7}",
//                model.getModelName(), question
//        );
//
//        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(openaiApiUrl, request, String.class);
//            JsonNode jsonNode = objectMapper.readTree(response.getBody());
//            String answer = jsonNode.path("choices").get(0).path("message").path("content").asText();
//            log.info("GPT ({}) 응답: {}", model.getModelName(), answer);
//            return CompletableFuture.completedFuture(answer);
//        } catch (HttpClientErrorException e) {
//            log.error("Error while calling OpenAI API: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
//            return CompletableFuture.completedFuture("");
//        } catch (Exception e) {
//            log.error("Unexpected error while calling OpenAI API: {}", e.getMessage());
//            return CompletableFuture.completedFuture("");
//        }
    }

    /*private CompletableFuture<String> getAnswerFromGemini(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey); // "Authorization" 대신 "x-goog-api-key" 사용

        String requestBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                question
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String answer = jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            log.info("Gemini 응답: {}", answer);
            return CompletableFuture.completedFuture(answer);
        } catch (HttpClientErrorException e) {
            log.error("Error while calling Gemini API: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return CompletableFuture.completedFuture("");
        } catch (Exception e) {
            log.error("Unexpected error while calling Gemini API: {}", e.getMessage());
            return CompletableFuture.completedFuture("");
        }
    }*/

    private CompletableFuture<String> getAnswerFromGemini(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.9);
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 1);
        generationConfig.put("maxOutputTokens", 2048);

        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        parts.add(Collections.singletonMap("text", question));
        content.put("parts", parts);
        contents.add(content);

        requestBody.put("contents", contents);
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Gemini API URL: {}", geminiApiUrl);
            log.info("Gemini API Key (first 5 characters): {}", geminiApiKey.substring(0, 5));
            log.info("Request Headers: {}", headers);
            log.info("Request Body: {}", objectMapper.writeValueAsString(requestBody));

            ResponseEntity<String> response = restTemplate.postForEntity(geminiApiUrl, request, String.class);

            log.info("Received response from Gemini API. Status code: {}", response.getStatusCode());
            log.debug("Response body: {}", response.getBody());

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String answer = jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            log.info("Gemini 응답: {}", answer);
            return CompletableFuture.completedFuture(answer);
        } catch (HttpClientErrorException e) {
            log.error("Error while calling Gemini API: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return CompletableFuture.failedFuture(new RuntimeException("Gemini API call failed: " + e.getStatusCode()));
        } catch (Exception e) {
            log.error("Unexpected error while calling Gemini API: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Unexpected error in Gemini API call", e));

    }
    }
}