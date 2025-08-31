package com.milsondev.servus.services;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.dtos.JwtResponseDTO;
import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.enums.Role;
import com.milsondev.servus.services.auth.AuthService;
import com.milsondev.servus.dtos.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrchestrationService {

    private final AuthService authService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ValidationService validationService;

    @Autowired
    public OrchestrationService(final AuthService authService,
                                final TokenService tokenService,
                                final UserRepository userRepository,
                                final ValidationService validationService) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.validationService = validationService;
    }

    public boolean isUserPresent(final String email) {
        return authService.isUserPresent(email);
    }

    public JwtResponseDTO login(final LoginRequestDTO loginRequestDTO) {
        return authService.login(loginRequestDTO);
    }

    public com.milsondev.servus.enums.ResetRequestStatus requestPasswordReset(final String email) {
        return authService.requestPasswordReset(email);
    }

    public void registerUser(final UserDTO userDto) {
        if (userDto.getRole() == null) {
            userDto.setRole(Role.ROLE_USER);
        }
        authService.register(userDto);
    }

    public boolean activateAccountFromToken(final String token) {
        String email = tokenService.getEmailFromToken(token);
        return authService.activateUserAccount(email);
    }

    public void validatePasswordResetToken(final String token) {
        tokenService.getEmailFromToken(token);
    }

    public JwtResponseDTO resetPasswordWithToken(final String token, final String newPassword) {
        return authService.resetPasswordWithToken(token, newPassword);
    }

    public Optional<UserEntity> findUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    public void updateUserProfile(final String email, final String fullName, final String phone) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        user.setFullName(fullName != null ? fullName.trim() : null);
        user.setPhone(phone != null ? phone.trim() : null);
        userRepository.save(user);
    }

    public void requestPasswordResetAsync(final String email) {
        authService.requestPasswordResetAsync(email);
    }

    public String getEmailFromToken(final String token) {
        return tokenService.getEmailFromToken(token);
    }

    // Validation helpers (delegated via ValidationService)
    public boolean isBlank(final String s) {
        return validationService.isBlank(s);
    }

    public String safeTrim(final String s) {
        return validationService.safeTrim(s);
    }

    public boolean isValidEmailFormat(final String email) {
        return validationService.isValidEmailFormat(email);
    }

    public boolean isStrongPassword(final String password) {
        return validationService.isStrongPassword(password);
    }
}