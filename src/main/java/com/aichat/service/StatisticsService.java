package com.aichat.service;

import com.aichat.entity.User;
import com.aichat.repository.ChatRepository;
import com.aichat.repository.MessageRepository;
import com.aichat.repository.UserRepository;
import com.aichat.repository.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatisticsService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserSessionRepository userSessionRepository;

    public StatisticsService(UserRepository userRepository,
                             ChatRepository chatRepository,
                             MessageRepository messageRepository,
                             UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userSessionRepository = userSessionRepository;
    }

    public List<Map<String, Object>> getStatistics() {
        List<Map<String, Object>> statistics = new ArrayList<>();

        List<User> users = userRepository.findAll();
        for (User user : users) {
            Map<String, Object> userStats = new HashMap<>();
            String username = user.getUsername();
            long totalChats = chatRepository.countByUser(user);
            long totalMessages = messageRepository.countByChatUser(user);
            long totalLogs = totalChats + totalMessages;

            userStats.put("username", username);
            userStats.put("totalLogs", totalLogs);
            userStats.put("totalChats", totalChats);
            userStats.put("totalMessages", totalMessages);

            statistics.add(userStats);
        }

        return statistics;
    }

    public Map<String, Long> getOverallStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalChats", chatRepository.count());
        stats.put("totalMessages", messageRepository.count());
        stats.put("activeSessions", userSessionRepository.count());
        return stats;
    }
}