package com.aichat.controller;

import com.aichat.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/admin/statistics")
    public String showStatistics(Model model) {
        model.addAttribute("statistics", statisticsService.getStatistics());
        return "statistics";
    }
}