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

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;


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
            System.err.println("❌ Erreur lors de l’envoi du mail Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
