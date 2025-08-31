package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.services.OrchestrationService;
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

    private final OrchestrationService orchestrationService;

    public ProfileController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @GetMapping
    public String profile(Model model) {
        String email = currentEmail();
        Optional<UserEntity> opt = orchestrationService.findUserByEmail(email);
        if (opt.isEmpty()) {
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
        Optional<UserEntity> opt = orchestrationService.findUserByEmail(email);
        if (opt.isEmpty()) {
            return "redirect:/login";
        }
        UserEntity user = opt.get();
        // Simple validation
        if (fullName == null || fullName.trim().isEmpty()) {
            ra.addFlashAttribute("error", "profile.fullName.required");
            return "redirect:/profile";
        }
        user.setFullName(fullName.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        orchestrationService.updateUserProfile(email, fullName, phone);
        ra.addFlashAttribute("success", "profile.update.success");
        return "redirect:/profile";
    }

    @PostMapping("/password-reset")
    public String requestPasswordReset(RedirectAttributes ra) {
        String email = currentEmail();
        orchestrationService.requestPasswordResetAsync(email);
        ra.addFlashAttribute("success", "profile.password.reset.requested.success");
        return "redirect:/profile";
    }

    private String currentEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
