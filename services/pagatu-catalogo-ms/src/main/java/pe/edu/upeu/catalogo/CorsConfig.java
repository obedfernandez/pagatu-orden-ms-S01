package pe.edu.upeu.catalogo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

/** Only for S11 3.2 first step. Leave disabled after enabling the Gateway. */
@Configuration
@ConditionalOnProperty(name = "pagatu.cors.enabled", havingValue = "true")
public class CorsConfig implements WebMvcConfigurer {
    @Value("${pagatu.cors.allowed-origins:http://localhost:4200}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toArray(String[]::new))
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
