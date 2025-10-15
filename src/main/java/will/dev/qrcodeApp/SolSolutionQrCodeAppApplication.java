package will.dev.qrcodeApp;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "will.dev.qrcodeApp")
public class SolSolutionQrCodeAppApplication {

    @Value("${app.env.apiUrl}")
    private String apiUrl;

    @Value("${app.base.url}")
    private String baseUrl;

    public static void main(String[] args) {
        SpringApplication.run(SolSolutionQrCodeAppApplication.class, args);
    }

    @PostConstruct
    public void testDBEnvVars() {
        System.out.println("===================================");
        System.out.println("DB URL: " + System.getenv("DATABASE_URL"));
        System.out.println("DB USER: " + System.getenv("DATABASE_USERNAME"));
        System.out.println("MAIL_USERNAME = " + System.getProperty("MAIL_USERNAME"));
        System.out.println("👉 lienDuSite: " + apiUrl);
        System.out.println("👉 BaseUrl: " + baseUrl);
        System.out.println("===================================");
    }
}
