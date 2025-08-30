package com.milsondev.servus.controllers;

import com.milsondev.servus.dtos.*;
import com.milsondev.servus.enums.Role;
import com.milsondev.servus.services.UserService;
import com.milsondev.servus.services.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
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
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    // tratar do reset pwd
    // login logout
    // tests

    private final AuthService authService;
    private final MessageSource messageSource;
    private final com.milsondev.servus.services.TokenService tokenService;

    private final UserService userService;

    public AuthController(AuthService authService, MessageSource messageSource, com.milsondev.servus.services.TokenService tokenService, UserService userService) {
        this.authService = authService;
        this.messageSource = messageSource;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginGet(@RequestParam(value = "lang", required = false) String lang) {
        if (lang != null && !lang.isBlank()) {
            String encoded = UriUtils.encode(lang, StandardCharsets.UTF_8);
            return "redirect:/login?lang=" + encoded;
        }
        return "redirect:/login";
    }

    @GetMapping("/password-reset")
    public String passwordResetGet(@RequestParam(value = "lang", required = false) String lang) {
        if (lang != null && !lang.isBlank()) {
            String encoded = UriUtils.encode(lang, StandardCharsets.UTF_8);
            return "redirect:/password-reset?lang=" + encoded;
        }
        return "redirect:/password-reset";
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

    @PostMapping(value = "/password-reset", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String resetPasswordForm(@ModelAttribute("reset") @Valid final ResetPasswordRequestDTO resetDto,
                                    final BindingResult result,
                                    final Model model) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            return "password-reset";
        }
        authService.requestPasswordResetAsync(resetDto.getEmail());
        return "redirect:/password-reset/requested";
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
            userDto.setRole(Role.ROLE_USER);
            authService.register(userDto);
            return "redirect:/sign-up/success";
        } catch (IllegalArgumentException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("_global", ex.getMessage());
            model.addAttribute("errors", errors);
            return "sign-up";
        }
    }

    @GetMapping("/active/token")
    public String activateAccount(@RequestParam("token") String token) {
        try {
            String email = tokenService.getEmailFromToken(token);
            boolean activated = authService.activateUserAccount(email);
            if (!activated) {
                return "redirect:/activation/failed";
            }
            return "redirect:/login?registered"; 
        } catch (Exception ex) {
            return "redirect:/activation/failed";
        }
    }

    @GetMapping("/password-reset/token")
    public String passwordResetWithToken(@RequestParam("token") String token) {
        try {
            String email = tokenService.getEmailFromToken(token);
            org.slf4j.LoggerFactory.getLogger(AuthController.class).info("Password reset token validated for {}", email);
            String encoded = UriUtils.encode(token, StandardCharsets.UTF_8);
            return "redirect:/password-reset/new?token=" + encoded;
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AuthController.class).warn("Password reset token invalid: {}", ex.getMessage());
            return "redirect:/password-reset?error=invalid_token";
        }
    }

    @PostMapping(value = "/password-reset/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String saveNewPassword(@ModelAttribute("newPassword") @Valid final NewPasswordDTO dto,
                                  final BindingResult result,
                                  final Model model,
                                  final HttpServletResponse httpResponse) {
        Map<String, String> errors = new HashMap<>();
        if (result.hasErrors()) {
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            model.addAttribute("errors", errors);
            model.addAttribute("token", dto.getToken());
            return "password-reset-new";
        }
        try {
            JwtResponseDTO jwt = authService.resetPasswordWithToken(dto.getToken(), dto.getPassword());

            var cookie = new jakarta.servlet.http.Cookie("Authorization", jwt.token());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setMaxAge(60 * 60 * 8);
            httpResponse.addCookie(cookie);
            return "redirect:/appointments";
        } catch (Exception ex) {
            errors.put("_global", "Unable to reset password: " + ex.getMessage());
            model.addAttribute("errors", errors);
            model.addAttribute("token", dto.getToken());
            return "password-reset-new";
        }
    }

    @GetMapping("/logout")
    public String logout(final HttpServletResponse httpResponse) {
        var cookie = new jakarta.servlet.http.Cookie("Authorization", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set true if using HTTPS
        cookie.setMaxAge(0); // delete cookie
        httpResponse.addCookie(cookie);
        return "redirect:/login?logout";
    }
}