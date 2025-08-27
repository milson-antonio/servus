package com.milsondev.servus.services.auth;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.dtos.JwtResponseDTO;
import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.enums.EmailTemplateType;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.services.email.EmailService;
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

import static com.milsondev.servus.enums.EmailTemplateType.RESET_PASSWORD;

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
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        return userOptional.map(UserEntity::isActive).orElse(false);
    }

    public boolean isUserPresent(final  String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void register(UserDTO userDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setFullName(userDto.getFullName());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setPhone(userDto.getPhone());
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setRole(userDto.getRole());
        LOGGER.info("Registering user: {}", userEntity);
        userRepository.save(userEntity);
        sendActivationEmailAsync(userEntity.getEmail());
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
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            LOGGER.info("User account with email {} not found and cannot be activated", email);
            return false;
        }

        UserEntity userEntity = userOptional.get();
        if (userEntity.isActive()) {
            LOGGER.info("User account with email {} is already active", email);
            return true;
        }
        userEntity.setActive(true);
        userRepository.save(userEntity);
        LOGGER.info("User account with email {} activated", email);
        return true;
    }
}