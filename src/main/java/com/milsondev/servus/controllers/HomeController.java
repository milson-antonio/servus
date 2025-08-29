package com.milsondev.servus.controllers;

import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.ResetPasswordRequestDTO;
import com.milsondev.servus.dtos.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean userLoggedIn = auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()));
        if (userLoggedIn) {
            return "redirect:/appointments";
        }
        return "redirect:/login";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("showUserHeader", true);
        return "appointments";
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("login")) {
            model.addAttribute("login", new LoginRequestDTO());
        }
        return "login";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        return "sign-up";
    }

    @GetMapping("/sign-up/success")
    public String signUpSuccess(Model model) {
        return "sign-up-success";
    }

    @GetMapping("/password-reset")
    public String passwordReset(Model model) {
        if (!model.containsAttribute("reset")) {
            model.addAttribute("reset", new ResetPasswordRequestDTO());
        }
        return "password-reset";
    }

    @GetMapping("/password-reset/new")
    public String passwordResetNew(Model model) {
        return "password-reset-new";
    }

    @GetMapping("/password-reset/requested")
    public String passwordResetRequested(Model model) {
        return "password-reset-requested";
    }

    @GetMapping("/activation/failed")
    public String activationFailed(Model model) {
        return "activation-failed";
    }

}