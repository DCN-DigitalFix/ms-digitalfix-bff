package cl.duoc.ms_digitalfix_bff.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Valores de application.yaml bajo el prefijo "digitalfix". */
@ConfigurationProperties(prefix = "digitalfix")
public record SecurityProperties(Security security, Cors cors) {

    public record Security(List<String> audiences) {}

    public record Cors(List<String> allowedOrigins) {}
}