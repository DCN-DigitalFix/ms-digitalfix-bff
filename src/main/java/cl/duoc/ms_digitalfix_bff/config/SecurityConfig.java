package cl.duoc.ms_digitalfix_bff.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import cl.duoc.ms_digitalfix_bff.exception.JsonAccessDeniedHandler;
import cl.duoc.ms_digitalfix_bff.exception.JsonAuthenticationEntryPoint;

/**
 * El BFF actua como OAuth 2.0 Resource Server: no emite tokens, los valida.
 *
 * La validacion es local, contra las claves publicas que Entra ID publica en su
 * endpoint JWKS. No hay llamada a Microsoft en cada request.
 *
 * Se validan cuatro cosas: firma, emisor (iss), audiencia (aud) y vigencia (exp).
 * Las tres primeras las arma este archivo; exp la revisa Spring por defecto.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final SecurityProperties properties;
    private final String issuerUri;

    @Autowired
    public SecurityConfig(
            SecurityProperties properties,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.properties = properties;
        this.issuerUri = issuerUri;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Sin sesiones ni formularios: CSRF no aplica a una API con Bearer token.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // El preflight de CORS viaja sin token: debe pasar.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Sonda publica para verificar que el servicio esta arriba.
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().authenticated())

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(new JwtAuthoritiesConverter()))
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                        .accessDeniedHandler(new JsonAccessDeniedHandler()));

        return http.build();
    }

    /**
     * Decodificador con validacion explicita de emisor y audiencia.
     * JwtDecoders.fromIssuerLocation descubre el JWKS leyendo el documento
     * de metadatos OIDC del tenant.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience =
                new AudienceValidator(properties.security().audiences());

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));

        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origenes explicitos, no "*": la rubrica penaliza los sobrepermisos.
        config.setAllowedOrigins(properties.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}