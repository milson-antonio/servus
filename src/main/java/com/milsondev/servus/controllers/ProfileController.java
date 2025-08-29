package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.services.auth.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public ProfileController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public String profile(Model model) {
        String email = currentEmail();
        Optional<UserEntity> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            // If for some reason not found, redirect to login
            return "redirect:/login";
        }
        UserEntity user = opt.get();
        model.addAttribute("user", user);
        model.addAttribute("showUserHeader", true);
        return "profile";
    }

    @PostMapping
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "phone", required = false) String phone,
                                RedirectAttributes ra) {
        String email = currentEmail();
        Optional<UserEntity> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return "redirect:/login";
        }
        UserEntity user = opt.get();
        // Simple validation
        if (fullName == null || fullName.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Full name is required");
            return "redirect:/profile";
        }
        user.setFullName(fullName.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        userRepository.save(user);
        ra.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/profile";
    }

    @PostMapping("/password-reset")
    public String requestPasswordReset(RedirectAttributes ra) {
        String email = currentEmail();
        authService.requestPasswordResetAsync(email);
        ra.addFlashAttribute("success", "Password reset email requested. Please check your inbox.");
        return "redirect:/profile";
    }

    private String currentEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
