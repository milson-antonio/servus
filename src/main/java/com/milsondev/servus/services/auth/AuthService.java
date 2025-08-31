package com.milsondev.servus.services.auth;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.dtos.JwtResponseDTO;
import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.enums.EmailTemplateType;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.services.TokenService;
import com.milsondev.servus.services.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import com.milsondev.servus.enums.ResetRequestStatus;

import static com.milsondev.servus.enums.EmailTemplateType.RESET_PASSWORD;

@Service
public class AuthService {

    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthService(EmailService emailService, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isEmpty()) {
            LOGGER.warn("Login attempt for non-existing account: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!userOpt.get().isActive()) {
            LOGGER.warn("Login attempt for inactive account: {}", loginRequest.getEmail());
            throw new IllegalArgumentException("Your account is not active. Please check your email to activate your account.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        int tokenVersion = userRepository.findByEmail(userDetails.getUsername()).map(u -> u.getTokenVersion() == null ? 0 : u.getTokenVersion()).orElse(0);
        String token = jwtUtil.generateToken(userDetails, tokenVersion);
        LOGGER.info("Login successful for user: {}", userDetails.getUsername());
        return new JwtResponseDTO(userDetails.getUsername(), token);
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


    public ResetRequestStatus requestPasswordReset(final String recipient) {
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(recipient);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                long now = System.currentTimeMillis();
                long last = user.getLastPasswordResetRequestAt() == null ? 0L : user.getLastPasswordResetRequestAt().getTime();
                long FIVE_MIN = 5L * 60L * 1000L;
                if (now - last < FIVE_MIN) {
                    LOGGER.info("Password reset request throttled for user {} (requested too soon)", recipient);
                    return ResetRequestStatus.THROTTLED;
                }
                user.setLastPasswordResetRequestAt(new java.util.Date(now));
                userRepository.save(user);
                emailService.sendEmail(recipient, RESET_PASSWORD);
                LOGGER.info("Password reset requested for existing user: {}", recipient);
                return ResetRequestStatus.SENT;
            } else {
                LOGGER.info("Password reset requested for non-existing email: {}", recipient);
                return ResetRequestStatus.SENT;
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to process password reset request for {}: {}", recipient, ex.getMessage());
            return ResetRequestStatus.SENT;
        }
    }

    @Async
    public void requestPasswordResetAsync(final String recipient) {
        requestPasswordReset(recipient);
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

    public JwtResponseDTO resetPasswordWithToken(String token, String newPassword) {
        String email = tokenService.getEmailFromToken(token);
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        UserEntity user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
        userRepository.save(user);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPassword(),
                        java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
                );
        String jwt = jwtUtil.generateToken(userDetails, user.getTokenVersion());
        return new JwtResponseDTO(user.getEmail(), jwt);
    }
}