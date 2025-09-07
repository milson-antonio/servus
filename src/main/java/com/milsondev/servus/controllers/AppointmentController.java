package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.ServiceTypeEntity;
import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.db.repositories.ServiceTypeRepository;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.enums.ApplicantType;
import com.milsondev.servus.services.AppointmentService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final MessageSource messageSource;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository, ServiceTypeRepository serviceTypeRepository, MessageSource messageSource) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.messageSource = messageSource;
    }

    @GetMapping("/api/appointments/available-slots")
    @ResponseBody
    public ResponseEntity<Map<String, List<String>>> getAvailableSlots(@RequestParam("year") int year, @RequestParam("month") int month) {
        Map<String, List<String>> slots = appointmentService.getAvailableSlots(year, month);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("showUserHeader", true);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()))) {
            String email = auth.getName();
            UserEntity user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                UUID userId = user.getId();
                model.addAttribute("upcomingAppointments", appointmentService.listUpcomingByUser(userId));
                model.addAttribute("pastAppointments", appointmentService.listPastByUser(userId));
                model.addAttribute("firstName", user.getFirstName());
            }
        }
        return "appointments";
    }

    @PostMapping("/schedule-appointment-time")
    public String scheduleAppointmentTime(@RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        String date = allParams.get("date");
        String time = allParams.get("time");

        allParams.forEach(ra::addAttribute);

        if (date == null || date.isBlank()) {
            String msg = messageSource.getMessage("schedule.datetime.error.dateNotSelected", null, LocaleContextHolder.getLocale());
            ra.addFlashAttribute("error", msg);
            return "redirect:/schedule-date-and-time";
        }

        if (time == null || time.isBlank()) {
            String msg = messageSource.getMessage("schedule.datetime.error.timeNotSelected", null, LocaleContextHolder.getLocale());
            ra.addFlashAttribute("error", msg);
            ra.addFlashAttribute("selectedDate", date);
            return "redirect:/schedule-date-and-time";
        }

        return "redirect:/schedule-confirm-details";
    }

    @PostMapping("/appointments")
    public String createAppointment(@RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || (auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()))) {
            ra.addFlashAttribute("error", "auth.required");
            return "redirect:/login";
        }
        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "user.notfound");
            return "redirect:/login";
        }
        UUID userId = user.getId();

        String serviceName = allParams.get("service");
        ServiceTypeEntity serviceType = serviceTypeRepository.findByName(serviceName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service type: " + serviceName));

        String applicantTypeStr = allParams.get("applicantType");
        String dateStr = allParams.get("date");
        String timeStr = allParams.get("time");

        ApplicantType applicantType = ApplicantType.fromInput(applicantTypeStr);
        Date startAt = deriveStartAt(dateStr, timeStr);
        Date endAt = null; // service will default to +30min if needed

        boolean forOther = applicantType == ApplicantType.OTHER;
        Map<String, String> otherPersonDetails = new HashMap<>();
        if (forOther) {
            otherPersonDetails.put("firstName", allParams.get("otherFirstName"));
            otherPersonDetails.put("lastName", allParams.get("otherLastName"));
            otherPersonDetails.put("dob", allParams.get("otherDob"));
            otherPersonDetails.put("nationality", allParams.get("otherNationality"));
            otherPersonDetails.put("passportNumber", allParams.get("otherPassportNumber"));
            otherPersonDetails.put("email", allParams.get("otherEmail"));
            otherPersonDetails.put("phone", allParams.get("otherPhone"));
        }

        try {
            var saved = appointmentService.createForUser(userId, serviceType, applicantType, startAt, endAt, forOther, otherPersonDetails);
            ra.addFlashAttribute("appointmentServiceName", serviceType.getLabel());
            ra.addFlashAttribute("appointmentDate", new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(saved.getStartAt()));
            ra.addFlashAttribute("appointmentTime", timeStr);
            ra.addFlashAttribute("appointmentLocation", "—");
            return "redirect:/appointment-confirmed";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            allParams.forEach(ra::addAttribute);
            return "redirect:/schedule-confirm-details";
        }
    }

    private Date deriveStartAt(String dateStr, String timeStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(dateStr.trim(), dateFormatter);
        LocalTime time = LocalTime.parse(timeStr.trim(), timeFormatter);

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}