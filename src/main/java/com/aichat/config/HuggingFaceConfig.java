package com.aichat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "huggingface")
@Data
public class HuggingFaceConfig {

    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}