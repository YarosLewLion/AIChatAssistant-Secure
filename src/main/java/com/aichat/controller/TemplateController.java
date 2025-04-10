package com.aichat.controller;

import com.aichat.entity.Template;
import com.aichat.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/templates")
public class TemplateController {
    @Autowired
    private TemplateService templateService;

    @GetMapping
    public String showTemplates(Model model) {
        model.addAttribute("templates", templateService.getAllTemplates());
        return "admin/templates";
    }

    @GetMapping("/new")
    public String showTemplateForm(Model model) {
        model.addAttribute("template", new Template());
        return "admin/template-form";
    }

    @PostMapping("/save")
    public String saveTemplate(@ModelAttribute Template template) {
        templateService.saveTemplate(template);
        return "redirect:/admin/templates";
    }

    @PostMapping("/delete/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return "redirect:/admin/templates";
    }
}
