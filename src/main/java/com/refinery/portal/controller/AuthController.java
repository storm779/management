package com.refinery.portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.refinery.portal.entity.User;
import com.refinery.portal.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model,
            Authentication authentication) {
        
        // If user is already authenticated, redirect to home
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }
        
        if (logout != null) {
            model.addAttribute("success", "You have been successfully logged out.");
        }
        
        return "auth/login";
    }
    
    /**
     * Show registration page
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model, Authentication authentication) {
        
        // If user is already authenticated, redirect to home
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    /**
     * Process user registration
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Registration attempt for username: {}", user.getUsername());
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        // Check if passwords match
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordError", "Passwords do not match.");
            return "auth/register";
        }
        
        // Check if username is available
        if (!userService.isUsernameAvailable(user.getUsername())) {
            model.addAttribute("usernameError", "Username is already taken.");
            return "auth/register";
        }
        
        // Check if email is available
        if (!userService.isEmailAvailable(user.getEmail())) {
            model.addAttribute("emailError", "Email is already registered.");
            return "auth/register";
        }
        
        try {
            // Register the user
            userService.registerUser(user);
            
            logger.info("User registered successfully: {}", user.getUsername());
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please login with your credentials.");
            
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            logger.error("Registration failed for username: {}", user.getUsername(), e);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            logger.error("Unexpected error during registration for username: {}", user.getUsername(), e);
            model.addAttribute("error", "Registration failed. Please try again.");
            return "auth/register";
        }
    }
    
    /**
     * Check username availability (AJAX endpoint)
     */
    @GetMapping("/api/check-username")
    @ResponseBody
    public boolean checkUsernameAvailability(@RequestParam String username) {
        return userService.isUsernameAvailable(username);
    }
    
    /**
     * Check email availability (AJAX endpoint)
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public boolean checkEmailAvailability(@RequestParam String email) {
        return userService.isEmailAvailable(email);
    }
    
    /**
     * Handle access denied
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model, HttpServletRequest request) {
        model.addAttribute("error", "You don't have permission to access this resource.");
        return "error/access-denied";
    }
} 