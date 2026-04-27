package will.dev.qrcodeApp.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import will.dev.qrcodeApp.entity.User;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class BrevoService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${app.env.apiUrl}")
    private String apiUrl;

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;


    /**
     * Envoyer un email de bienvenue avec les informations de connexion
     */
    public void sendWelcomeEmail(User user, String credentials, Boolean... optionnel) {
        try {
            // 1️⃣ Configuration du client Brevo
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // 2️⃣ Instanciation de l’API TransactionalEmailsApi
            TransactionalEmailsApi emailApi = new TransactionalEmailsApi();

            // 3️⃣ Création du contenu du mail
            String subject = "Bienvenue dans " + senderName + " - Vos informations de connexion";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("userEmail", user.getEmail());
            context.setVariable("codeAcces", user.getCodeAcces());
            context.setVariable("tempPassword", user.getPassword());
            context.setVariable("appName", senderName);
            context.setVariable("role", user.getRole().toString());

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            // 4️⃣ Configuration du mail
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(user.getEmail())));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            // 5️⃣ Envoi du mail
            emailApi.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès à " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer un email avec le QR code généré
     */
    public void sendQrCodeEmail(User user, String qrContent, byte[] qrBytes) {
        try {
            // 1️⃣ Configuration du client Brevo
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // 2️⃣ Instanciation de l’API TransactionalEmailsApi
            TransactionalEmailsApi emailApi = new TransactionalEmailsApi();

            // 3️⃣ Création du contenu du mail
            String subject = "Votre QR Code est prêt !";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("qrContent", qrContent);
            context.setVariable("appName", senderName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("qrcode-email", context);
            helper.setText(htmlContent, true);
            SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment()
                    .name("qrcode.png")
                    .content(qrBytes);

            // 4️⃣ Configuration du mail
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(user.getEmail())));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);
            sendSmtpEmail.attachment(Collections.singletonList(attachment));

            // 5️⃣ Envoi du mail
            emailApi.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès à " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer un email de notification de changement de rôle
     */
    public void sendRoleChangeNotification(User user, String oldRole, String newRole, String tempPassword) {
        try {
            // 1️⃣ Configuration du client Brevo
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // 2️⃣ Instanciation de l’API TransactionalEmailsApi
            TransactionalEmailsApi emailApi = new TransactionalEmailsApi();

            // 3️⃣ Création du contenu du mail
            String subject = "Modification de votre rôle - " + senderName ;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("oldRole", oldRole);
            context.setVariable("newRole", newRole);
            context.setVariable("tempPassword", tempPassword);
            context.setVariable("appName", senderName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("role-change-email", context);
            helper.setText(htmlContent, true);

            // 4️⃣ Configuration du mail
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(user.getEmail())));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            // 5️⃣ Envoi du mail
            emailApi.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès à " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer un email de notification de changement de statut
     */
    public void sendStatusChangeNotification(User user, boolean isActive) {
        try {
            // 1️⃣ Configuration du client Brevo
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // 2️⃣ Instanciation de l’API TransactionalEmailsApi
            TransactionalEmailsApi emailApi = new TransactionalEmailsApi();

            // 3️⃣ Création du contenu du mail
            String subject = (isActive ? "Activation" : "Désactivation") + " de votre compte - " + senderName ;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", user.getNom());
            context.setVariable("isActive", isActive);
            context.setVariable("appName", senderName);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("status-change-email", context);
            helper.setText(htmlContent, true);

            // 4️⃣ Configuration du mail
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(user.getEmail())));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            // 5️⃣ Envoi du mail
            emailApi.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès à " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String email, String userName, String resetToken) {
        try {
            // 1️⃣ Configuration du client Brevo
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            // 2️⃣ Instanciation de l’API TransactionalEmailsApi
            TransactionalEmailsApi emailApi = new TransactionalEmailsApi(defaultClient);

            // 3️⃣ Création du contenu du mail
            String subject = "Réinitialisation de votre mot de passe - " + senderName ;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Créer le contexte pour le template
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("resetToken", resetToken);
            context.setVariable("appName", senderName);
            context.setVariable("resetUrl", apiUrl+"/reset-password?token=" + resetToken);

            // Générer le contenu HTML
            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);

            // 4️⃣ Configuration du mail
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender()
                    .email(senderEmail)
                    .name(senderName));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(email)));
            sendSmtpEmail.setSubject(subject);
            sendSmtpEmail.setHtmlContent(htmlContent);

            // 5️⃣ Envoi du mail
            emailApi.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ Email envoyé avec succès à " + email);

        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
