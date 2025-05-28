package com.aichat.repository;

import com.aichat.entity.Message;
import com.aichat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.user = :user")
    long countByChatUser(User user);
}