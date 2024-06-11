package com.example.elsa;

import com.example.elsa.domain.qna.service.StandardService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitialDataLoader {
    private final StandardService standardService;

    @Bean
    CommandLineRunner loadInitialData() {
        return args -> {
            List<String> initialStandards = List.of(
                    "프라이버시", "데이터관리", "책임성", "안전성", "투명성", "다양성",
                    "침해금지", "연대성", "공공성", "인권보장"
            );
            standardService.addInitialStandards(initialStandards);
        };
    }
}
