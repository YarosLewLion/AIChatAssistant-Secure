package com.aichat.config;

   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.context.annotation.Configuration;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;

   @Configuration
   public class HuggingFaceConfig {

       private static final Logger logger = LoggerFactory.getLogger(HuggingFaceConfig.class);

       @Value("${xai-api-key:}")
       private String xaiApiKey;

       public HuggingFaceConfig() {
           logger.info("xAI API key loaded from Spring property (xai-api-key): {}", xaiApiKey);
           logger.info("XAI_API_KEY from System.getenv: {}", System.getenv("XAI_API_KEY"));
           logger.info("xai-api-key from System.getProperty: {}", System.getProperty("xai-api-key"));
       }

       public String getXaiApiKey() {
           return xaiApiKey;
       }
   }