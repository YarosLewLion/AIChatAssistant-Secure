package com.aichat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "huggingface")
@Data
public class HuggingFaceConfig {
    private String apiKey;
}
