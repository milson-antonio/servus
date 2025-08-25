package com.milsondev.servus.controller;

import com.milsondev.servus.dto.JwtResponseDTO;
import com.milsondev.servus.dto.LoginRequestDTO;
import com.milsondev.servus.dto.UserDTO;
import com.milsondev.servus.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        try {
            JwtResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String registerForm(@ModelAttribute("user") @Valid final UserDTO userDto,
                               final BindingResult result,
                               final Model model) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error: result.getFieldErrors()){
                errors.put(error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            return "sign-up";
        }

        try {
            authService.register(userDto);
            return "redirect:/sign-up/success";
        } catch (IllegalArgumentException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("_global", ex.getMessage());
            model.addAttribute("errors", errors);
            return "sign-up";
        }
    }
}