package com.aichat.service;

import com.aichat.entity.Template;
import com.aichat.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public void saveTemplate(Template template) {
        templateRepository.save(template);
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }
}