package com.milsondev.servus.controller;

import com.milsondev.servus.dto.JwtResponseDTO;
import com.milsondev.servus.dto.LoginRequestDTO;
import com.milsondev.servus.dto.UserDTO;
import com.milsondev.servus.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
        try {
            JwtResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> registerForm(@Valid UserDTO userDto, @RequestHeader(value = "HX-Request", required = false) String hxRequest) {
        try {
            authService.register(userDto);
            if (("true".equalsIgnoreCase(hxRequest) || "yes".equalsIgnoreCase(hxRequest))) {
                return ResponseEntity.ok().header("HX-Redirect", "/sign-up/success").build();
            }
            return ResponseEntity.status(303).location(URI.create("/sign-up/success")).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}