package com.aichat.security;

import com.aichat.entity.User;
import com.aichat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Попытка загрузки пользователя: " + username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            System.out.println("Пользователь не найден: " + username);
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }
        System.out.println("Пользователь найден: " + user.getUsername());
        System.out.println("Зашифрованный пароль в базе: " + user.getPassword());
        return user;
    }
}