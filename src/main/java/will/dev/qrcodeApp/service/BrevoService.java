package will.dev.qrcodeApp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
import java.util.List;
import java.util.Map;

@Service
public class BrevoService {

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:SSAC QR Code App}")
    private String appName;

    @Value("${app.env.apiUrl}")
    private String apiUrl;

    @Value("${app.env.apiKey}")
    private String brevoApiKey;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Envoie un e-mail HTML avec QR code en pièce jointe via Brevo (API)
     */
    public void sendQrCodeEmail(User user, String qrCodeUrl, String qrContent) {
        try {
            // ⚙️ Configuration de l'API Brevo
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(brevoApiKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            // 👤 Expéditeur et 📩 Destinataire (inchangé)
            SendSmtpEmailSender sender = new SendSmtpEmailSender().name(appName).email(fromEmail);
            SendSmtpEmailTo recipient = new SendSmtpEmailTo().email(user.getEmail()).name(user.getNom());

            // --- DÉBUT DE LA MODIFICATION CLÉ ---
            // 💾 Téléchargement de l'image du QR code depuis son URL
            byte[] qrBytes = restTemplate.getForObject(qrCodeUrl, byte[].class);
            String qrBase64 = "";

            if (qrBytes != null && qrBytes.length > 0) {
                // Encodage des octets téléchargés en Base64
                qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
            } else {
                System.err.println("⚠️ Le téléchargement du QR code depuis l'URL a échoué ou le fichier est vide.");
            }
            // --- FIN DE LA MODIFICATION CLÉ ---

            // 🎨 Contenu HTML du message (inchangé)
            String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif; color:#333;">
                    <h2>Bonjour %s 👋</h2>
                    <p>Voici votre QR Code généré par <strong>%s</strong>.</p>
                    <p>Contenu du QR : <code>%s</code></p>
                    <p>Vous trouverez le QR code en pièce jointe.</p>
                    <hr/>
                    <small>Ceci est un message automatique, merci de ne pas répondre.</small>
                </body>
            </html>
            """.formatted(user.getNom(), appName, qrContent);

            // 📎 Pièce jointe (logique inchangée, mais maintenant plus fiable)
            List<SendSmtpEmailAttachment> attachments = null;
            if (!qrBase64.isEmpty()) {
                attachments = List.of(
                        new SendSmtpEmailAttachment()
                                .name("qrcode.png")
                                .content(qrBase64.getBytes())
                                //.contentType("image/png") // Optionnel mais recommandé
                );
            }

            // 📨 Construction et 🚀 Envoi de l'email (inchangé)
            SendSmtpEmail email = new SendSmtpEmail()
                    .sender(sender)
                    .to(List.of(recipient))
                    .subject("Votre QR Code généré - " + appName)
                    .htmlContent(htmlContent)
                    .attachment(attachments);

            apiInstance.sendTransacEmail(email);
            System.out.println("✅ Email Brevo envoyé à " + user.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi Brevo : " + e.getMessage());
            throw new RuntimeException("Erreur d'envoi de l'email via Brevo", e);
        }
    }
}
