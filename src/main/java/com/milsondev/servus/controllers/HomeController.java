package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.OtherPersonDetailsDTO;
import com.milsondev.servus.dtos.ResetPasswordRequestDTO;
import com.milsondev.servus.dtos.NewPasswordDTO;
import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.enums.AppointmentServiceType;
import com.milsondev.servus.services.OrchestrationService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/")
public class HomeController {

    private final OrchestrationService orchestrationService;

    public HomeController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @GetMapping
    public String home(Model model) {
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

    @GetMapping("/schedule-documents")
    public String scheduleDocuments(@RequestParam(name = "service") String service, Model model) {
        model.addAttribute("showUserHeader", true);
        model.addAttribute("service", service);
        return "schedule-documents";
    }

    @GetMapping("/schedule-who")
    public String scheduleWho(@RequestParam(name = "service") String service,
                              Model model) {
        model.addAttribute("showUserHeader", true);
        model.addAttribute("service", service);
        return "schedule-who";
    }

    @GetMapping("/schedule-for-other")
    public String scheduleForOther(@RequestParam(name = "service", required = false) String service, Model model) {
        if (service == null || service.isBlank()) {
            return "redirect:/schedule-appointment";
        }

        if (!model.containsAttribute("otherPersonDetails")) {
            OtherPersonDetailsDTO dto = new OtherPersonDetailsDTO();
            dto.setService(service);
            model.addAttribute("otherPersonDetails", dto);
        }
        model.addAttribute("showUserHeader", true);
        return "schedule-for-other";
    }

    @PostMapping("/schedule-for-other")
    public String postScheduleForOther(@Valid @ModelAttribute("otherPersonDetails") OtherPersonDetailsDTO otherPersonDetails,
                                       BindingResult bindingResult,
                                       RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.otherPersonDetails", bindingResult);
            ra.addFlashAttribute("otherPersonDetails", otherPersonDetails);
            ra.addAttribute("service", otherPersonDetails.getService());
            return "redirect:/schedule-for-other";
        }

        ra.addAttribute("service", otherPersonDetails.getService());
        ra.addAttribute("applicantType", "OTHER");
        ra.addAttribute("otherFirstName", otherPersonDetails.getOtherFirstName());
        ra.addAttribute("otherLastName", otherPersonDetails.getOtherLastName());
        ra.addAttribute("otherDob", otherPersonDetails.getOtherDob());
        ra.addAttribute("otherNationality", otherPersonDetails.getOtherNationality());
        ra.addAttribute("otherPassportNumber", otherPersonDetails.getOtherPassportNumber());
        ra.addAttribute("otherEmail", otherPersonDetails.getOtherEmail());
        ra.addAttribute("otherPhone", otherPersonDetails.getOtherPhone());

        return "redirect:/schedule-date-and-time";
    }


    @GetMapping("/schedule-date-and-time")
    public String scheduleDateAndTime(@RequestParam Map<String, String> allParams, Model model) {
        model.addAttribute("showUserHeader", true);
        allParams.forEach(model::addAttribute);
        return "schedule-date-and-time";
    }

    @GetMapping("/schedule-confirm-details")
    public String scheduleConfirmDetails(@RequestParam Map<String, String> allParams, Model model) {
        model.addAttribute("showUserHeader", true);

        // Add full user object if appointment is for SELF
        if ("SELF".equals(allParams.get("applicantType"))) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            orchestrationService.findUserByEmail(email).ifPresent(user -> model.addAttribute("user", user));
        }
        
        // Format service name for display
        String serviceName = allParams.get("service");
        if (serviceName != null) {
            AppointmentServiceType serviceType = AppointmentServiceType.fromInput(serviceName);
            if (serviceType != null) {
                model.addAttribute("serviceLabel", serviceType.getLabel());
            }
        }

        reformatDate(allParams, "date");
        allParams.forEach(model::addAttribute);
        return "schedule-confirm-details";
    }

    private void reformatDate(Map<String, String> params, String key) {
        if (params.containsKey(key) && params.get(key) != null && !params.get(key).isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate parsedDate = LocalDate.parse(params.get(key), inputFormatter);
                params.put(key, parsedDate.format(outputFormatter));
            } catch (Exception e) {
                System.err.println("Erro ao converter data: " + params.get(key));
            }
        }
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