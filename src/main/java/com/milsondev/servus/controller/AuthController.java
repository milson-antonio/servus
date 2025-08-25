package com.milsondev.servus.controller;

import com.milsondev.servus.dto.JwtResponseDTO;
import com.milsondev.servus.dto.LoginRequestDTO;
import com.milsondev.servus.dto.UserDTO;
import com.milsondev.servus.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final MessageSource messageSource;

    public AuthController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        return "sign-up";
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String loginForm(@ModelAttribute("login") @Valid final LoginRequestDTO loginDto,
                            final BindingResult result,
                            final Model model,
                            final HttpServletResponse httpResponse) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            return "login";
        }
        try {
            JwtResponseDTO jwt = authService.login(loginDto);
            var cookie = new jakarta.servlet.http.Cookie("Authorization", jwt.token());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // true if behind HTTPS
            cookie.setMaxAge(60 * 60 * 8); // 8 hours
            httpResponse.addCookie(cookie);
            return "redirect:/appointments";
        } catch (BadCredentialsException ex) {
            Map<String, String> errors = new HashMap<>();
            String msg = messageSource.getMessage("login.error.invalidCredentials", null, LocaleContextHolder.getLocale());
            errors.put("_global", msg);
            model.addAttribute("errors", errors);
            return "login";
        } catch (IllegalArgumentException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("_global", ex.getMessage());
            model.addAttribute("errors", errors);
            return "login";
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