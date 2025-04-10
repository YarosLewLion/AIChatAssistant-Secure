package com.aichat.controller;

import com.aichat.entity.Chat;
import com.aichat.entity.User;
import com.aichat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/chat")
    public String chat(Model model, @AuthenticationPrincipal User user) {
        List<Chat> chats = chatService.getUserChats(user);
        if (chats.isEmpty()) {
            chats.add(chatService.startNewChat(user));
        }
        Chat currentChat = chats.get(chats.size() - 1);
        model.addAttribute("chat", currentChat);
        model.addAttribute("messages", currentChat.getMessages());
        return "chat";
    }

    @PostMapping("/chat")
    public String sendMessage(
            @RequestParam("userMessage") String userMessage,
            @RequestParam("chatId") Long chatId,
            @AuthenticationPrincipal User user,
            Model model
    ) {
        Chat chat = chatService.getUserChats(user)
                .stream()
                .filter(c -> c.getId().equals(chatId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        chatService.saveMessage(chat, "user", userMessage);

        String botResponse = chatService.getBotResponse(userMessage, user);
        if (botResponse != null) {
            chatService.saveMessage(chat, "bot", botResponse);
        }

        model.addAttribute("chat", chat);
        model.addAttribute("messages", chat.getMessages());
        return "chat";
    }

    @PostMapping("/chat/new")
    public String newChat(@AuthenticationPrincipal User user, Model model) {
        Chat newChat = chatService.startNewChat(user);
        model.addAttribute("chat", newChat);
        model.addAttribute("messages", newChat.getMessages());
        return "chat";
    }
}