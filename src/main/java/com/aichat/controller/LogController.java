package com.aichat.controller;

import com.aichat.repository.LogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogController {

    private final LogRepository logRepository;

    public LogController(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/admin/logs")
    public String showLogs(Model model) {
        model.addAttribute("logs", logRepository.findAll());
        return "logs";
    }
}