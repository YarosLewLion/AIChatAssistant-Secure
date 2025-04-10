package com.aichat.controller;

import com.aichat.entity.Chat;
import com.aichat.entity.User;
import com.aichat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatApiController {

    @Autowired
    private ChatService chatService;

    @GetMapping
    public List<Chat> getUserChats(@AuthenticationPrincipal User user) {
        return chatService.getUserChats(user);
    }
}