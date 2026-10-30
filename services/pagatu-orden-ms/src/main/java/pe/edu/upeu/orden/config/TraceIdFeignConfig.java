package pe.edu.upeu.orden.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Propaga el identificador de correlacion HTTP de Orden a Catalogo por Feign. */
@Configuration
public class TraceIdFeignConfig {

    @Bean
    public RequestInterceptor traceIdRequestInterceptor() {
        return template -> {
            String traceId = MDC.get("traceId");
            if (traceId != null && !traceId.isBlank()) {
                template.header("X-Trace-ID", traceId);
            }
        };
    }
}
