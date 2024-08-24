package com.example.elsa.domain.qna.service;

import com.example.elsa.domain.qna.dto.ChatRequest;
import com.example.elsa.domain.qna.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    @Qualifier("openaiRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Async("taskExecutor")
    public CompletableFuture<String> getAnswerFromGPT(String question) {
        ChatRequest request = new ChatRequest("gpt-3.5-turbo", question, 250);

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
    }
}