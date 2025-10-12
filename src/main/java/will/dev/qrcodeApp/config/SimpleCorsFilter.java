package will.dev.qrcodeApp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE ) // TRÈS IMPORTANT : garantit que ce filtre s'exécute en premier
public class SimpleCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // On ajoute les en-têtes CORS à CHAQUE réponse
        response.setHeader("Access-Control-Allow-Origin", "https://sol-solution-qrcode.vercel.app" );
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*"); // Autorise tous les en-têtes demandés
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Si c'est une requête preflight (OPTIONS), on répond simplement "OK" et on arrête le traitement.
        // La requête n'ira pas plus loin (pas de Spring Security, pas de contrôleur).
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Pour toutes les autres requêtes (GET, POST, etc.), on continue la chaîne de filtres normale.
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Pas besoin d'implémentation ici
    }

    @Override
    public void destroy() {
        // Pas besoin d'implémentation ici
    }
}

