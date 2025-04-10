package com.aichat;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "testpassword";
        String encodedPassword = encoder.encode(password);
        System.out.println("Зашифрованный пароль: " + encodedPassword);
    }
}
