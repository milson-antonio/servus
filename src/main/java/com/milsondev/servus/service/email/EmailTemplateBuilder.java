package com.milsondev.servus.service.email;

import com.milsondev.servus.enu.EmailTemplateType;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateBuilder {

    private final String configuredLogoUrl;

    public EmailTemplateBuilder(org.springframework.core.env.Environment env) {
        this.configuredLogoUrl = env.getProperty("logo.url");
    }

    public static class EmailContent {
        private final String subject;
        private final String htmlBody;
        public EmailContent(String subject, String htmlBody) {
            this.subject = subject;
            this.htmlBody = htmlBody;
        }
        public String getSubject() { return subject; }
        public String getHtmlBody() { return htmlBody; }
    }

    private String wrapHtml(String title, String bodyInner) {
        int year = java.time.Year.now().getValue();
        return """
            <!DOCTYPE html>
            <html lang=\"en\">
            <head>
              <meta charset=\"UTF-8\" />
              <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
              <title>%s</title>
              <style>
                body { margin:0; padding:0; background:#f5f7fb; font-family: Arial, Helvetica, sans-serif; color:#0d141c; }
                .container { max-width:900px; margin:0 auto; padding:24px; }
                .card { background:#ffffff; border-radius:12px; padding:24px; border:1px solid #e6eaf2; box-shadow:0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04); }
                .header { display:flex; align-items:center; gap:12px; margin-bottom:8px; }
                .brand { font-size:14px; color:#49739c; font-weight:600; }
                .btn { display:inline-block; padding:12px 18px; background:#6b5cf6; color:#ffffff !important; text-decoration:none; border-radius:8px; font-weight:bold; }
                .muted { color:#49739c; font-size:13px; }
                .copy { word-break:break-all; background:#f5f7fb; padding:10px; border-radius:8px; font-size:12px; color:#0d141c; }
                .footer { text-align:center; margin-top:16px; color:#49739c; font-size:12px; }
                img.logo { height:28px; width:auto; display:block; }
              </style>
            </head>
            <body>
              <div class=\"container\">
                <div class=\"card\">
                  %s
                </div>
              </div>
            </body>
            </html>
        """.formatted(title, bodyInner, year);
    }

    public EmailContent build(EmailTemplateType type, String token, String baseUrl, String brandName) {
        switch (type) {
            case ACTIVATE_ACCOUNT: {
                String activationLink = baseUrl + "/auth/active/token?token=" + token;
                String subject = "Action Required: Activate your account at Angolan Embassy Appointments Portal!";
                String inner = """
                  <div style=\"text-align:center;\">
                    <h1>Welcome to Angolan Embassy Appointments Portal!</h1>
                    <p>Thank you for registering with Angolan Embassy. We're excited to help you schedule your embassy visit with ease.</p>
                    <p>To activate your account, please click the link below:</p>
                    <p><a class=\"btn\" style=\"background:#1a73e8\" href=\"%s\" target=\"_blank\" rel=\"noopener\">Activate Account</a></p>
                    <p class=\"muted\">If the button above doesn't work, you can also copy and paste the following link into your browser:</p>
                    <p class=\"copy\">%s</p>
                    <p class=\"muted\">Once your account is activated, you can log in and start scheduling your appointment. If you have any questions or need assistance, please don't hesitate to contact our support team.</p>
                    <p>Thank you, The Angolan Embassy Appointment Team</p>
                  </div>
                """.formatted(brandName, activationLink, activationLink);
                String html = wrapHtml(subject, inner);
                return new EmailContent(subject, html);
            }
            case RESET_PASSWORD: {
                String resetLink = baseUrl + "/auth/password-reset/token?token=" + token;
                String subject = "Reset your password";
                String inner = """
                  <h2>Reset your password</h2>
                  <p>We received a request to reset your password. Click the button below to continue:</p>
                  <p><a class=\"btn\" href=\"%s\" target=\"_blank\" rel=\"noopener\">Reset Password</a></p>
                  <p class=\"muted\">Can’t click the button? Copy and paste this link into your browser:</p>
                  <p class=\"copy\">%s</p>
                  <p class=\"muted\">If you did not request a password reset, you can ignore this email.</p>
                """.formatted(resetLink, resetLink);
                String html = wrapHtml(subject, inner);
                return new EmailContent(subject, html);
            }
            default:
                throw new IllegalArgumentException("Unsupported email template type: " + type);
        }
    }
}