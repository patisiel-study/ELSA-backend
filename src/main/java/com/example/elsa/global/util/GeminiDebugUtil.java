package com.example.elsa.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class GeminiDebugUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logGeminiRequest(String question, String fullUrl, Object requestBody) {
        try {
            log.info("=== Gemini API Request Debug ===");
            log.info("Question: {}", question);
            log.info("Full URL: {}", fullUrl);
            log.info("Request Body: {}", objectMapper.writeValueAsString(requestBody));
        } catch (Exception e) {
            log.error("Error logging Gemini request", e);
        }
    }

    public void logGeminiResponse(ResponseEntity<String> response) {
        try {
            log.info("=== Gemini API Response Debug ===");
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Headers: {}", response.getHeaders());

            String responseBody = response.getBody();
            log.info("Raw Response: {}", responseBody);

            // Parse and log structured response
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("error")) {
                log.error("Gemini API Error: {}", jsonNode.get("error"));
            }

            JsonNode candidates = jsonNode.path("candidates");
            if (candidates.isArray()) {
                for (JsonNode candidate : candidates) {
                    log.info("Candidate Content: {}", candidate.path("content").path("parts").path(0).path("text"));
                    if (candidate.has("finishReason")) {
                        log.info("Finish Reason: {}", candidate.get("finishReason"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error logging Gemini response", e);
        }
    }

    public boolean isResponseValid(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode candidates = jsonNode.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.error("No candidates in response");
                return false;
            }

            JsonNode content = candidates.get(0).path("content");
            if (content.isMissingNode()) {
                log.error("Missing content in first candidate");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Error validating Gemini response", e);
            return false;
        }
    }
}