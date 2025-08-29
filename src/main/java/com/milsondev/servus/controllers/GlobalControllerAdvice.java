package com.milsondev.servus.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()));
        model.addAttribute("showUserHeader", authenticated);
        if (authenticated) {
            // Expose basic info that may be useful in templates (optional)
            model.addAttribute("currentUserEmail", auth.getName());
        }
    }
}
