package com.aichat.service;

import com.aichat.config.HuggingFaceConfig;
import com.aichat.entity.Chat;
import com.aichat.entity.Log;
import com.aichat.entity.Message;
import com.aichat.entity.User;
import com.aichat.repository.ChatRepository;
import com.aichat.repository.LogRepository;
import com.aichat.repository.MessageRepository;
import com.aichat.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final HuggingFaceConfig huggingFaceConfig;
    private final ObjectMapper objectMapper;

    public ChatService(ChatRepository chatRepository,
                       MessageRepository messageRepository,
                       LogRepository logRepository,
                       UserRepository userRepository,
                       RestTemplate restTemplate,
                       HuggingFaceConfig huggingFaceConfig,
                       ObjectMapper objectMapper) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.huggingFaceConfig = huggingFaceConfig;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<Chat> getUserChats(User user) {
        logger.debug("Fetching chats for user: {}", user.getUsername());
        return chatRepository.findByUser(user);
    }

    @Transactional
    public Chat startNewChat(User user) {
        logger.debug("Starting new chat for user: {}", user.getUsername());
        User managedUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getUsername()));
        Chat chat = new Chat(managedUser);
        logAction(managedUser, "Started new chat");
        Chat savedChat = chatRepository.save(chat);
        logger.debug("New chat created with ID: {}", savedChat.getId());
        return savedChat;
    }

    @Transactional
    public void saveMessage(Chat chat, String sender, String text) {
        logger.debug("Saving message for chat ID: {}. Sender: {}, Text: {}", chat.getId(), sender, text);
        Message message = new Message(chat, sender, text);
        messageRepository.save(message);
        chat.getMessages().add(message);
        chatRepository.save(chat);
        logAction(chat.getUser(), sender.equals("user") ? "Sent message: " + text : "Received bot response: " + text);
        logger.debug("Message saved successfully");
    }

    @Transactional
    public void endChat(Chat chat) {
        logger.debug("Ending chat with ID: {}", chat.getId());
        chat.setEndTime(LocalDateTime.now());
        chatRepository.save(chat);
        logAction(chat.getUser(), "Ended chat with ID: " + chat.getId());
    }

    public String getBotResponse(String userMessage, User user) {
        logger.info("Received message from user {}: {}", user.getUsername(), userMessage);
        String xaiResponse = getXaiResponse(userMessage);
        logger.info("Response from xAI API: {}", xaiResponse);
        return xaiResponse;
    }

    private String getXaiResponse(String userMessage) {
        String token = huggingFaceConfig.getXaiApiKey();
        if (token == null || token.trim().isEmpty()) {
            logger.warn("xAI API key is not configured. Returning default response.");
            return "This is a test response from the bot (xAI API key missing).";
        }

        String apiUrl = "https://api.x.ai/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        logger.debug("Using xAI API token: {}", token);
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");

        String requestBody = "{\"messages\": [{\"role\": \"user\", \"content\": \"" + userMessage + "\"}], \"max_tokens\": 1000}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            logger.debug("Sending request to xAI API: {}", requestBody);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            logger.debug("Received response from xAI API: {}", response.getBody());
            JsonNode root = objectMapper.readTree(response.getBody());
            
            JsonNode usage = root.path("usage");
            int promptTokens = usage.path("prompt_tokens").asInt();
            int completionTokens = usage.path("completion_tokens").asInt();
            int totalTokens = usage.path("total_tokens").asInt();
            logger.info("Token usage - Prompt tokens: {}, Completion tokens: {}, Total tokens: {}", 
                        promptTokens, completionTokens, totalTokens);

            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            logger.error("Error while calling xAI API: {}", e.getMessage(), e);
            return "Sorry, I couldn't generate a response. Please try again.";
        }
    }

    @Transactional
    private void logAction(User user, String action) {
        logger.debug("Logging action for user {}: {}", user.getUsername(), action);
        Log log = new Log();
        log.setUser(user);
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        logRepository.save(log);
    }
}