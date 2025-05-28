package com.aichat.repository;

import com.aichat.entity.Chat;
import com.aichat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUser(User user);
    long countByUser(User user);
}