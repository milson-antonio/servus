package com.milsondev.servus.services;

import com.milsondev.servus.services.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrchestrationService {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public OrchestrationService(final UserService userService, final AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    public boolean isUserPresent(final  String email) {
        return authService.isUserPresent(email);
    }

}