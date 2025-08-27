package com.milsondev.servus.service.auth;

import com.milsondev.servus.dto.JwtResponseDTO;
import com.milsondev.servus.dto.LoginRequestDTO;
import com.milsondev.servus.dto.UserDTO;
import com.milsondev.servus.entity.User;
import com.milsondev.servus.enu.EmailTemplateType;
import com.milsondev.servus.repository.UserRepository;
import com.milsondev.servus.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.milsondev.servus.enu.EmailTemplateType.RESET_PASSWORD;

@Service
public class AuthService {

    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthService(EmailService emailService, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        if (!isUserPresentAndAccountActive(loginRequest.getEmail())) {
            LOGGER.warn("Login attempt for inactive or non-existing account: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Your account is not active. Please check your email to activate your account.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        LOGGER.info("Login successful for user: {}", userDetails.getUsername());
        return new JwtResponseDTO(userDetails.getUsername(), token);
    }

    public boolean isUserPresentAndAccountActive(final String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(User::isActive).orElse(false);
    }

    public boolean isUserPresent(final  String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void register(UserDTO userDto) {
        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());
        LOGGER.info("Registering user: {}", user);
        userRepository.save(user);
        sendActivationEmailAsync(user.getEmail());
    }

    @Async
    public void requestPasswordResetAsync(final String recipient) {
        try {
            boolean exists = isUserPresent(recipient);
            if (exists) {
                emailService.sendEmail(recipient, RESET_PASSWORD);
                LOGGER.info("Password reset requested for existing user: {}", recipient);
            } else {
                LOGGER.info("Password reset requested for non-existing email: {}", recipient);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to process password reset request for {}: {}", recipient, ex.getMessage());
        }
    }

    @Async
    public void sendActivationEmailAsync(final String recipient) {
        try {
            boolean exists = isUserPresent(recipient);
            if (exists) {
                emailService.sendEmail(recipient, EmailTemplateType.ACTIVATE_ACCOUNT);
                LOGGER.info("Activation requested for existing user: {}", recipient);
            } else {
                LOGGER.info("Activation requested for non-existing email: {}", recipient);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to process activation request for {}: {}", recipient, ex.getMessage());
        }
    }

    public boolean activateUserAccount(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            LOGGER.info("User account with email {} not found and cannot be activated", email);
            return false;
        }

        User user = userOptional.get();
        if (user.isActive()) {
            LOGGER.info("User account with email {} is already active", email);
            return true;
        }
        user.setActive(true);
        userRepository.save(user);
        LOGGER.info("User account with email {} activated", email);
        return true;
    }
}