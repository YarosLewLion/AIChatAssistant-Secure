package com.aichat.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chat chat;

    private String sender;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    public Message() {
    }

    public Message(Chat chat, String sender, String text) {
        this.chat = chat;
        this.sender = sender;
        this.text = text;
    }
}