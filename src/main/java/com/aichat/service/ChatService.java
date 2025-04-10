package com.aichat.service;

import com.aichat.entity.Chat;
import com.aichat.entity.Message;
import com.aichat.entity.User;
import com.aichat.repository.ChatRepository;
import com.aichat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Value("${huggingface.api.token}")
    private String huggingFaceApiToken;

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
        System.out.println("Получено сообщение от пользователя: " + userMessage);

        String huggingFaceResponse = getHuggingFaceResponse(userMessage);
        System.out.println("Ответ от Hugging Face API: " + huggingFaceResponse);
        return huggingFaceResponse;
    }

    private String getHuggingFaceResponse(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingFaceApiToken);
        headers.set("Content-Type", "application/json");

        String requestBody = "{\"inputs\": \"" + userMessage + "\", \"parameters\": {\"max_length\": 100, \"temperature\": 0.7, \"top_p\": 0.9}}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            int textStart = responseBody.indexOf("\"generated_text\":\"") + 17;
            int textEnd = responseBody.indexOf("\"}", textStart);
            String generatedText = responseBody.substring(textStart, textEnd);
            return generatedText;
        } catch (Exception e) {
            System.out.println("Ошибка при вызове Hugging Face API: " + e.getMessage());
            e.printStackTrace();
            return "Извините, я не смог сгенерировать ответ. Попробуйте снова.";
        }
    }
}