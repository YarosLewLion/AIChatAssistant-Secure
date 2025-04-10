package com.aichat.service;

import com.aichat.entity.Chat;
import com.aichat.entity.Message;
import com.aichat.entity.Template;
import com.aichat.entity.User;
import com.aichat.entity.UserPreference;
import com.aichat.repository.ChatRepository;
import com.aichat.repository.MessageRepository;
import com.aichat.repository.TemplateRepository;
import com.aichat.repository.UserPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChatService chatService;

    private User user;
    private Chat chat;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "password", "ROLE_USER");
        user.setId(1L);
        chat = new Chat(user);
        chat.setId(1L);

        lenient().when(chatRepository.save(any(Chat.class))).thenReturn(chat);
        lenient().when(messageRepository.save(any(Message.class))).thenReturn(new Message());
    }

    @Test
    void testGetBotResponse_ExactMatch() {
        String userMessage = "привет";
        Template template = new Template("привет", "Привет! Чем могу помочь?");
        when(templateRepository.findByKeyword(userMessage.toLowerCase())).thenReturn(Optional.of(template));

        String response = chatService.getBotResponse(userMessage, user);

        assertEquals("Привет! Чем могу помочь?", response);
        verify(templateRepository, times(1)).findByKeyword(userMessage.toLowerCase());
    }

    @Test
    void testGetBotResponse_SubstringMatch() {
        String userMessage = "тест";
        Template template = new Template("другой", "Это тестовый ответ");
        when(templateRepository.findByKeyword(userMessage.toLowerCase())).thenReturn(Optional.empty());
        when(templateRepository.findByResponseContaining(userMessage.toLowerCase())).thenReturn(Arrays.asList(template));

        String response = chatService.getBotResponse(userMessage, user);

        assertEquals("Это тестовый ответ", response);
        verify(templateRepository, times(1)).findByKeyword(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findByResponseContaining(userMessage.toLowerCase());
    }

    @Test
    void testGetBotResponse_LevenshteinMatch() {
        String userMessage = "привт";
        Template template = new Template("привет", "Привет! Чем могу помочь?");
        when(templateRepository.findByKeyword(userMessage.toLowerCase())).thenReturn(Optional.empty());
        when(templateRepository.findByResponseContaining(userMessage.toLowerCase())).thenReturn(Collections.emptyList());
        when(templateRepository.findAll()).thenReturn(Arrays.asList(template));

        String response = chatService.getBotResponse(userMessage, user);

        assertEquals("Привет! Чем могу помочь?", response);
        verify(templateRepository, times(1)).findByKeyword(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findByResponseContaining(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findAll();
    }

    @Test
    void testGetBotResponse_PreferenceMatch() {
        String userMessage = "что-то";
        UserPreference preference = new UserPreference(user, "interest", "тест");
        Template template = new Template("другой", "Это тестовый ответ");
        when(templateRepository.findByKeyword(userMessage.toLowerCase())).thenReturn(Optional.empty());
        when(templateRepository.findByResponseContaining(userMessage.toLowerCase())).thenReturn(Collections.emptyList());
        when(templateRepository.findAll()).thenReturn(Collections.emptyList());
        when(userPreferenceRepository.findByUser(user)).thenReturn(Arrays.asList(preference));
        when(templateRepository.findByResponseContaining("тест")).thenReturn(Arrays.asList(template));

        String response = chatService.getBotResponse(userMessage, user);

        assertEquals("Кстати, раз тебе интересна тема 'тест', вот что я могу сказать: Это тестовый ответ", response);
        verify(templateRepository, times(1)).findByKeyword(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findByResponseContaining(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findAll();
        verify(userPreferenceRepository, times(1)).findByUser(user);
        verify(templateRepository, times(1)).findByResponseContaining("тест");
    }

    @Test
    void testGetBotResponse_HuggingFaceResponse() {
        String userMessage = "How are you?";
        String huggingFaceResponse = "{\"generated_text\":\"I'm doing well, thank you!\"}";
        when(templateRepository.findByKeyword(userMessage.toLowerCase())).thenReturn(Optional.empty());
        when(templateRepository.findByResponseContaining(userMessage.toLowerCase())).thenReturn(Collections.emptyList());
        when(templateRepository.findAll()).thenReturn(Collections.emptyList());
        when(userPreferenceRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(huggingFaceResponse));

        String response = chatService.getBotResponse(userMessage, user);

        assertEquals("I'm doing well, thank you!", response);
        verify(templateRepository, times(1)).findByKeyword(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findByResponseContaining(userMessage.toLowerCase());
        verify(templateRepository, times(1)).findAll();
        verify(userPreferenceRepository, times(1)).findByUser(user);
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.POST), any(), eq(String.class));
    }
}