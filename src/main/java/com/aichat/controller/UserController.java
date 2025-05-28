package com.aichat.controller;

import com.aichat.entity.User;
import com.aichat.service.ChatService;
import com.aichat.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ChatService chatService;

    public UserController(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String showHome(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("chats", chatService.getUserChats(user));
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(User user, Model model, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Account created successfully!");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/admin/users")
    public String showUsers(Model model) {
        logger.debug("Accessing user management page");
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long userId, RedirectAttributes redirectAttributes) {
        logger.debug("Attempting to delete user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete user. Please try again.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long userId, Model model) {
        logger.debug("Showing edit form for user with ID: {}", userId);
        User user = userService.findById(userId);
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/admin/users/edit/{id}")
    public String editUser(@PathVariable("id") Long userId, User user, RedirectAttributes redirectAttributes) {
        logger.debug("Editing user with ID: {}", userId);
        try {
            userService.updateUser(userId, user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user. Please try again.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/update-role/{id}")
    public String updateUserRole(@PathVariable("id") Long userId, @RequestParam("role") String role, RedirectAttributes redirectAttributes) {
        logger.debug("Attempting to update role for user with ID: {} to role: {}", userId, role);
        try {
            User user = userService.findById(userId);
            user.setRoles(role);
            userService.updateUser(userId, user);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully.");
        } catch (Exception e) {
            logger.error("Error updating role for user with ID {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user role. Please try again.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}