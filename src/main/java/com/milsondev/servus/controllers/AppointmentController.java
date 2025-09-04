package com.milsondev.servus.controllers;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.enums.ApplicantType;
import com.milsondev.servus.enums.AppointmentServiceType;
import com.milsondev.servus.services.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
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
            }
        }
        return "appointments";
    }

    @PostMapping("/appointments")
    public String createAppointment(@RequestParam(name = "service", required = false) String serviceStr,
                                    @RequestParam(name = "applicantType", required = false) String applicantTypeStr,
                                    @RequestParam(name = "date", required = false) String dateStr,
                                    @RequestParam(name = "time", required = false) String timeStr,
                                    RedirectAttributes ra) {
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

        // Map applicant type
        ApplicantType applicantType = ApplicantType.SELF;
        if (applicantTypeStr != null) {
            String s = applicantTypeStr.trim().toUpperCase(Locale.ROOT);
            if ("OTHER".equals(s) || "OUTRO".equals(s)) {
                try { applicantType = ApplicantType.valueOf("OTHER"); } catch (Exception ignored) {}
            } else {
                applicantType = ApplicantType.SELF;
            }
        }

        Date startAt = deriveStartAt(dateStr, timeStr);
        Date endAt = null; // service will default to +30min if needed

        // Parse service string into enum
        AppointmentServiceType service = AppointmentServiceType.fromInput(serviceStr);

        try {
            var saved = appointmentService.createForUser(userId, service, applicantType, startAt, endAt);
            // Prepare confirmation display fields
            ra.addFlashAttribute("appointmentServiceName", service != null ? service.getLabel() : "");
            ra.addFlashAttribute("appointmentDate", new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(saved.getStartAt()));
            ra.addFlashAttribute("appointmentTime", new SimpleDateFormat("h:mm a", Locale.ENGLISH).format(saved.getStartAt()));
            ra.addFlashAttribute("appointmentLocation", "—");
            return "redirect:/appointment-confirmed";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            // Send back to confirm page with the current selections
            String url = "/schedule-confirm-details" + buildQueryParams(serviceStr, applicantTypeStr, dateStr, timeStr);
            return "redirect:" + url;
        }
    }

    private String buildQueryParams(String service, String applicantType, String date, String time) {
        List<String> parts = new ArrayList<>();
        if (service != null && !service.isBlank()) parts.add("service=" + urlEncode(service));
        if (applicantType != null && !applicantType.isBlank()) parts.add("applicantType=" + urlEncode(applicantType));
        if (date != null && !date.isBlank()) parts.add("date=" + urlEncode(date));
        if (time != null && !time.isBlank()) parts.add("time=" + urlEncode(time));
        return parts.isEmpty() ? "" : ("?" + String.join("&", parts));
    }

    private String urlEncode(String v) {
        try { return java.net.URLEncoder.encode(v, java.nio.charset.StandardCharsets.UTF_8); } catch (Exception e) { return v; }
    }

    private Date deriveStartAt(String dateStr, String timeStr) {
        // Try multiple patterns
        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd",
                "MM/dd/yyyy HH:mm",
                "MM/dd/yyyy",
                "dd/MM/yyyy HH:mm",
                "dd/MM/yyyy",
                "h:mm a" // time-only, use today/tomorrow
        );
        Date datePart = null;
        Date timePart = null;
        if (dateStr != null && !dateStr.isBlank()) {
            for (String p : patterns) {
                if (!p.contains("HH") && !p.contains("h") && p.contains("yyyy")) {
                    try { datePart = new SimpleDateFormat(p).parse(dateStr.trim()); break; } catch (ParseException ignored) {}
                }
            }
        }
        if (timeStr != null && !timeStr.isBlank()) {
            for (String p : patterns) {
                if (!p.contains("yyyy")) {
                    try { timePart = new SimpleDateFormat(p, Locale.ENGLISH).parse(timeStr.trim()); break; } catch (ParseException ignored) {}
                }
            }
        }
        Calendar cal = Calendar.getInstance();
        if (datePart != null) {
            cal.setTime(datePart);
        } else {
            // default to tomorrow
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (timePart != null) {
            Calendar t = Calendar.getInstance();
            t.setTime(timePart);
            cal.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 10);
            cal.set(Calendar.MINUTE, 0);
        }
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}