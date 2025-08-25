package com.milsondev.servus.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

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
        return "appointments";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new com.milsondev.servus.dto.UserDTO());
        }
        return "sign-up";
    }

    @GetMapping("/sign-up/success")
    public String signUpSuccess(Model model) {
        return "sign-up-success";
    }

    @GetMapping("/password-reset")
    public String passwordReset(Model model) {
        return "password-reset";
    }

    @GetMapping("/password-reset/new")
    public String passwordResetNew(Model model) {
        return "password-reset-new";
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

}