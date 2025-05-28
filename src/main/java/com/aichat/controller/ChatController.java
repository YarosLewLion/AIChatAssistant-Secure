package com.aichat.controller;

import com.aichat.entity.Chat;
import com.aichat.entity.User;
import com.aichat.repository.UserRepository;
import com.aichat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String chat(Model model, @AuthenticationPrincipal User user) {
        logger.debug("Accessing chats for user: {}", user.getUsername());
        List<Chat> chats = chatService.getUserChats(user);
        if (chats.isEmpty()) {
            logger.debug("No chats found for user: {}. Starting a new chat.", user.getUsername());
            chats.add(chatService.startNewChat(user));
        }
        Chat currentChat = chats.get(chats.size() - 1);
        model.addAttribute("chat", currentChat);
        model.addAttribute("messages", currentChat.getMessages());
        return "chat";
    }

    @PostMapping("")
    public String sendMessage(
            @RequestParam("id") Long chatId,
            @RequestParam("message") String message,
            @AuthenticationPrincipal User user,
            Model model
    ) {
        logger.debug("Sending message to chat ID: {} for user: {}", chatId, user.getUsername());
        Chat chat = chatService.getUserChats(user)
                .stream()
                .filter(c -> c.getId().equals(chatId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        chatService.saveMessage(chat, "user", message);

        String botResponse = chatService.getBotResponse(message, user);
        if (botResponse != null) {
            chatService.saveMessage(chat, "bot", botResponse);
        }

        model.addAttribute("chat", chat);
        model.addAttribute("messages", chat.getMessages());
        return "chat";
    }

    @PostMapping("/new")
    public String newChat(@AuthenticationPrincipal User user, Model model) {
        logger.debug("Starting a new chat for user: {}", user.getUsername());
        Chat newChat = chatService.startNewChat(user);
        model.addAttribute("chat", newChat);
        model.addAttribute("messages", newChat.getMessages());
        return "chat";
    }

    @PostMapping("/end")
    public String endChat(@RequestParam("id") Long chatId, @AuthenticationPrincipal User user, Model model) {
        logger.debug("Ending chat with ID: {} for user: {}", chatId, user.getUsername());
        try {
            Chat chat = chatService.getUserChats(user).stream()
                    .filter(c -> c.getId().equals(chatId))
                    .findFirst()
                    .orElse(null);
            if (chat == null) {
                logger.warn("Chat with ID {} not found for user {}", chatId, user.getUsername());
                model.addAttribute("error", "Chat not found or you don't have access to it.");
                return "home";
            }
            if (chat.getEndTime() != null) {
                logger.warn("Chat with ID {} is already ended for user {}", chatId, user.getUsername());
                model.addAttribute("error", "Chat is already ended.");
                return "home";
            }
            chatService.endChat(chat);
            logger.info("Chat with ID {} successfully ended for user {}", chatId, user.getUsername());
            model.addAttribute("success", "Chat ended successfully.");
            return "redirect:/home";
        } catch (Exception e) {
            logger.error("Error ending chat with ID {} for user {}: {}", chatId, user.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Something went wrong while ending the chat. Please try again.");
            return "home";
        }
    }

    @PostMapping("/grant-admin")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> grantAdminRole(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        Map<String, Boolean> response = new HashMap<>();

        if ("Kingdom Hearts".equals(password)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            String currentRoles = user.getRoles();
            if (currentRoles.contains("ROLE_ADMIN")) {
                response.put("success", true);
                logger.info("User {} is already an admin.", username);
            } else {
                user.setRoles("ROLE_ADMIN");
                userRepository.save(user);
                logger.info("User {} has been granted admin role.", username);

                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    user,
                    authentication.getCredentials(),
                    user.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);

                response.put("success", true);
            }
        } else {
            logger.warn("Incorrect admin password attempt for user: {}", SecurityContextHolder.getContext().getAuthentication().getName());
            response.put("success", false);
        }

        return ResponseEntity.ok(response);
    }
}