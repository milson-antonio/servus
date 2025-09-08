package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.dtos.LoginRequestDTO;
import com.milsondev.servus.dtos.OtherPersonDetailsDTO;
import com.milsondev.servus.dtos.ResetPasswordRequestDTO;
import com.milsondev.servus.dtos.NewPasswordDTO;
import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.services.OrchestrationService;
import com.milsondev.servus.services.ServiceTypeService;
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
    private final ServiceTypeService serviceTypeService;

    public HomeController(OrchestrationService orchestrationService, ServiceTypeService serviceTypeService) {
        this.orchestrationService = orchestrationService;
        this.serviceTypeService = serviceTypeService;
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
        model.addAttribute("services", serviceTypeService.findAll());
        return "schedule-appointment";
    }

    @GetMapping("/schedule-documents")
    public String scheduleDocuments(@RequestParam(name = "service") String service, Model model) {
        model.addAttribute("showUserHeader", true);
        model.addAttribute("service", service);

        serviceTypeService.findByName(service).ifPresent(serviceType -> {
            model.addAttribute("serviceLabel", serviceType.getLabel());
            model.addAttribute("documents", serviceType.getRequiredDocuments());
        });
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
    public String scheduleConfirmDetails(@RequestParam(name = "service") String service,
                                         @RequestParam(name = "applicantType") String applicantType,
                                         @RequestParam(name = "date") String date,
                                         @RequestParam(name = "time") String time,
                                         @RequestParam(name = "otherFirstName", required = false) String otherFirstName,
                                         @RequestParam(name = "otherLastName", required = false) String otherLastName,
                                         @RequestParam(name = "otherDob", required = false) String otherDob,
                                         @RequestParam(name = "otherNationality", required = false) String otherNationality,
                                         @RequestParam(name = "otherPassportNumber", required = false) String otherPassportNumber,
                                         @RequestParam(name = "otherEmail", required = false) String otherEmail,
                                         @RequestParam(name = "otherPhone", required = false) String otherPhone,
                                         Model model) {
        model.addAttribute("showUserHeader", true);

        if ("SELF".equals(applicantType)) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            orchestrationService.findUserByEmail(email).ifPresent(user -> model.addAttribute("user", user));
        }

        serviceTypeService.findByName(service).ifPresent(serviceType -> 
            model.addAttribute("serviceLabel", serviceType.getLabel()));

        model.addAttribute("service", service);
        model.addAttribute("applicantType", applicantType);
        model.addAttribute("date", reformatDate(date));
        model.addAttribute("time", time);
        model.addAttribute("otherFirstName", otherFirstName);
        model.addAttribute("otherLastName", otherLastName);
        model.addAttribute("otherDob", otherDob);
        model.addAttribute("otherNationality", otherNationality);
        model.addAttribute("otherPassportNumber", otherPassportNumber);
        model.addAttribute("otherEmail", otherEmail);
        model.addAttribute("otherPhone", otherPhone);

        return "schedule-confirm-details";
    }

    private String reformatDate(final String date) {
        if (date != null && !date.isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
                return parsedDate.format(outputFormatter);
            } catch (Exception e) {
                System.err.println("Erro ao converter data: " + date);
            }
        }
        return null;
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