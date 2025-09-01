package com.milsondev.servus.controllers;

import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.ResetPasswordRequestDTO;
import com.milsondev.servus.dtos.NewPasswordDTO;
import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.services.OrchestrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class HomeController {

    private final OrchestrationService orchestrationService;

    public HomeController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @GetMapping
    public String home(Model model) {
        //userService.deleteAllUsers();
        return "index";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean userLoggedIn = auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()));
        if (userLoggedIn) {
            return "redirect:/appointments";
        }
        return "redirect:/login";
    }


    @GetMapping("/schedule-appointment")
    public String scheduleAppointment(Model model) {
        model.addAttribute("showUserHeader", true);
        return "schedule-appointment";
    }

    @GetMapping("/schedule-who")
    public String scheduleWho(@RequestParam(name = "service", required = false) String service,
                              @RequestParam(name = "applicantType", required = false) String applicantType,
                              Model model) {
        model.addAttribute("showUserHeader", true);
        if (service != null && !service.isBlank()) model.addAttribute("service", service);
        if (applicantType != null && !applicantType.isBlank()) model.addAttribute("applicantType", applicantType);
        return "schedule-who";
    }

    @GetMapping("/schedule-documents")
    public String scheduleDocuments(@RequestParam(name = "service", required = false) String service, Model model) {
        model.addAttribute("showUserHeader", true);
        if (service != null && !service.isBlank()) {
            model.addAttribute("service", service);
        }
        return "schedule-documents";
    }

    @GetMapping("/schedule-date-and-time")
    public String scheduleDateAndTime(@RequestParam(name = "service", required = false) String service,
                                      @RequestParam(name = "applicantType", required = false) String applicantType,
                                      @RequestParam(name = "date", required = false) String date,
                                      @RequestParam(name = "time", required = false) String time,
                                      Model model) {
        model.addAttribute("showUserHeader", true);
        if (service != null && !service.isBlank()) model.addAttribute("service", service);
        if (applicantType != null && !applicantType.isBlank()) model.addAttribute("applicantType", applicantType);
        if (date != null && !date.isBlank()) model.addAttribute("date", date);
        if (time != null && !time.isBlank()) model.addAttribute("time", time);
        return "schedule-date-and-time";
    }

    @GetMapping("/schedule-confirm-details")
    public String scheduleConfirmDetails(@RequestParam(name = "service", required = false) String service,
                                         @RequestParam(name = "applicantType", required = false) String applicantType,
                                         @RequestParam(name = "date", required = false) String date,
                                         @RequestParam(name = "time", required = false) String time,
                                         Model model) {
        model.addAttribute("showUserHeader", true);
        if (service != null && !service.isBlank()) model.addAttribute("service", service);
        if (applicantType != null && !applicantType.isBlank()) model.addAttribute("applicantType", applicantType);
        if (date != null && !date.isBlank()) model.addAttribute("date", date);
        if (time != null && !time.isBlank()) model.addAttribute("time", time);
        return "schedule-confirm-details";
    }

    @GetMapping("/appointment-confirmed")
    public String appointmentConfirmed(Model model) {
        model.addAttribute("showUserHeader", true);
        return "appointment-confirmed";
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("login")) {
            model.addAttribute("login", new LoginRequestDTO());
        }
        return "login";
    }

    @GetMapping("/sign-up")
    public String signUp(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        return "sign-up";
    }

    @GetMapping("/sign-up/success")
    public String signUpSuccess(Model model) {
        return "sign-up-success";
    }

    @GetMapping("/password-reset")
    public String passwordReset(Model model) {
        if (!model.containsAttribute("reset")) {
            model.addAttribute("reset", new ResetPasswordRequestDTO());
        }
        return "password-reset";
    }

    @GetMapping("/password-reset/new")
    public String passwordResetNew(@RequestParam(value = "token", required = false) String token, Model model) {
        if (!model.containsAttribute("newPassword")) {
            var dto = new NewPasswordDTO();
            if (token != null && !token.isBlank()) {
                dto.setToken(token);
            }
            model.addAttribute("newPassword", dto);
        }
        if (token != null && !token.isBlank()) {
            model.addAttribute("token", token);
        }
        return "password-reset-new";
    }

    @GetMapping("/password-reset/requested")
    public String passwordResetRequested(Model model) {
        return "password-reset-requested";
    }

    @GetMapping("/activation/failed")
    public String activationFailed(Model model) {
        return "activation-failed";
    }

}