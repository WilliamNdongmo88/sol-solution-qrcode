package will.dev.qrcodeApp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import will.dev.qrcodeApp.entity.User;

import java.io.File;
import java.util.Collections;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:SSAC QR Code App}")
    private String appName;

    @Value("${app.env.apiUrl}")
    private String apiUrl;

    /**
     * Envoyer un email de bienvenue avec les informations de connexion
     */
    public void sendWelcomeEmail(User user, String credentials, Boolean... optionnel) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Bienvenue dans " + appName + " - Vos informations de connexion");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("userEmail", user.getEmail());
            context.setVariable("codeAcces", user.getCodeAcces());
            context.setVariable("tempPassword", user.getPassword());//tempPassword : A revoir
            context.setVariable("appName", appName);
            context.setVariable("role", user.getRole().toString());

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de bienvenue", e);
        }
    }

    /**
     * Envoyer un email avec le QR code généré
     */
    public void sendQrCodeEmail(User user, String qrCodePath, String qrContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Votre QR Code généré - " + appName);

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("qrContent", qrContent);
            context.setVariable("appName", appName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("qrcode-email", context);
            helper.setText(htmlContent, true);

            // Attacher le fichier QR code
            File qrFile = new File(qrCodePath);
            if (qrFile.exists()) {
                FileSystemResource file = new FileSystemResource(qrFile);
                helper.addAttachment("qrcode.png", file);
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec QR code", e);
        }
    }

    /**
     * Envoyer un email simple
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Envoyer un email de notification de changement de rôle
     */
    public void sendRoleChangeNotification(User user, String oldRole, String newRole, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Modification de votre rôle - " + appName);

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("oldRole", oldRole);
            context.setVariable("newRole", newRole);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("appName", appName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("role-change-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de changement de rôle", e);
        }
    }

    /**
     * Envoyer un email de notification de changement de statut
     */
    public void sendStatusChangeNotification(User user, boolean isActive) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject((isActive ? "Activation" : "Désactivation") + " de votre compte - " + appName);

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("isActive", isActive);
            context.setVariable("appName", appName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("status-change-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de changement de statut", e);
        }
    }


    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String email, String userName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Réinitialisation de votre mot de passe - " + appName);

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetToken", resetToken);
            context.setVariable("appName", appName);
            context.setVariable("resetUrl", apiUrl+"/reset-password?token=" + resetToken);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation", e);
        }
    }
}


