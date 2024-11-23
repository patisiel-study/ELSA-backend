package com.example.elsa.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("https://humaind.co.kr", "http://localhost:3000") // 리액트 애플리케이션의 도메인 및 포트 번호
			.allowedMethods("*") // OPTIONS 메서드 추가
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600); // preflight 요청 캐싱 시간 (초)
	}

}