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
        Random r = new Random();
        boolean userLoggedIn = r.nextBoolean();
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
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String handleSignUp(
            @RequestParam(name = "fullName", required = false) String fullName,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "confirmPassword", required = false) String confirmPassword
    ) {
        // Minimal validation for demo purposes; no persistence yet
        boolean hasBasics = notBlank(fullName) && notBlank(email) && notBlank(password) && notBlank(confirmPassword);
        boolean passwordsMatch = notBlank(password) && password.equals(confirmPassword);
        if (!hasBasics || !passwordsMatch) {
            return "redirect:/sign-up?error=1";
        }
        // In a real app, create user, send verification, etc.
        return "redirect:/login?registered=1";
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

}