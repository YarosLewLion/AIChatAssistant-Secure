package com.aichat;

   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;

   @SpringBootApplication
   public class AiChatAssistantApplication {

       public static void main(String[] args) {
           String xaiApiKey = System.getenv("XAI_API_KEY");
           if (xaiApiKey != null && !xaiApiKey.trim().isEmpty()) {
               System.setProperty("xai-api-key", xaiApiKey);
               System.out.println("Set xai-api-key system property from XAI_API_KEY: " + xaiApiKey);
           } else {
               System.out.println("XAI_API_KEY environment variable not found.");
           }

           SpringApplication.run(AiChatAssistantApplication.class, args);
       }
   }