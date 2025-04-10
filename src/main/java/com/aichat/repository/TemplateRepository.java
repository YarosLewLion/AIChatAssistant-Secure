package com.aichat.repository;

import com.aichat.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByKeyword(String keyword);
    List<Template> findByResponseContaining(String response);
}