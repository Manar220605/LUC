package com.luc.qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class LucApplication {

    public static void main(String[] args) {
        SpringApplication.run(LucApplication.class, args);
    }
}
