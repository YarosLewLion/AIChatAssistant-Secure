package com.aichat.service;

import com.aichat.config.HuggingFaceConfig;
import com.aichat.entity.Chat;
import com.aichat.entity.Message;
import com.aichat.entity.User;
import com.aichat.repository.ChatRepository;
import com.aichat.repository.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Transactional
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;
    private final HuggingFaceConfig huggingFaceConfig;
    private final ObjectMapper objectMapper;

    public ChatService(ChatRepository chatRepository,
                       MessageRepository messageRepository,
                       RestTemplate restTemplate,
                       HuggingFaceConfig huggingFaceConfig) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;
        this.huggingFaceConfig = huggingFaceConfig;
        this.objectMapper = new ObjectMapper();
    }

    public List<Chat> getUserChats(User user) {
        return chatRepository.findByUser(user);
    }

    public Chat startNewChat(User user) {
        Chat chat = new Chat(user);
        return chatRepository.save(chat);
    }

    public void saveMessage(Chat chat, String sender, String text) {
        Message message = new Message(chat, sender, text);
        messageRepository.save(message);
        chat.getMessages().add(message);
        chatRepository.save(chat);
    }

    public String getBotResponse(String userMessage, User user) {
        logger.info("Received message from user: {}", userMessage);

        String huggingFaceResponse = getHuggingFaceResponse(userMessage);
        logger.info("Response from Hugging Face API: {}", huggingFaceResponse);
        return huggingFaceResponse;
    }

    private String getHuggingFaceResponse(String userMessage) {
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingFaceConfig.getApiKey());
        headers.set("Content-Type", "application/json");

        String requestBody = "{\"inputs\": \"" + userMessage + "\", \"parameters\": {\"max_length\": 100, \"temperature\": 0.7, \"top_p\": 0.9}}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get(0).get("generated_text").asText();
        } catch (Exception e) {
            logger.error("Error while calling Hugging Face API: {}", e.getMessage(), e);
            return "Sorry, I couldn't generate a response. Please try again.";
        }
    }
}