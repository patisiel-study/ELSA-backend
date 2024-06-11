package com.example.elsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElsaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElsaApplication.class, args);
    }

}
