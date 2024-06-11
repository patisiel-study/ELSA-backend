package com.example.elsa.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PythonExecutor {

    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> executeSentimentAnalysis(String answer) {
        try {
//            // 로컬: 가상 환경의 Python 인터프리터 경로 설정
//            String pythonInterpreter = "venv/bin/python"; // macOS/Linux

            // Dockerfile에서 설정한 가상 환경의 Python 인터프리터 경로 설정
            String pythonInterpreter = "/app/venv/bin/python"; // Docker 컨테이너 내부 경로

            // 로컬경로
           //  String scriptPath = "src/main/resources/python/sentiment_analysis.py";
            String scriptPath = "/app/python/sentiment_analysis.py"; // Python 스크립트의 경로
            ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, scriptPath, answer);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            reader.close();
            process.waitFor();

            String jsonOutput = output.toString();
            Map<String, Object> result = new ObjectMapper().readValue(jsonOutput, Map.class);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute sentiment analysis script", e);
        }
    }
}
