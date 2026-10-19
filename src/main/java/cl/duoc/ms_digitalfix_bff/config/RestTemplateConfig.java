package cl.duoc.ms_digitalfix_bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // El constructor sin argumentos deja a RestTemplate con
        // SimpleClientHttpRequestFactory, que por dentro es HttpURLConnection.
        // Ese cliente valida el metodo contra una lista fija donde PATCH no
        // figura, y lanza ProtocolException antes de abrir el socket. Como el
        // BFF reenvia los cambios de estado con PATCH, todos terminaban en 500.
        //
        // JdkClientHttpRequestFactory usa el java.net.http.HttpClient moderno,
        // que no filtra el metodo. No agrega dependencias: viene en el JDK.
        RestTemplate restTemplate = new RestTemplate(new JdkClientHttpRequestFactory());

        // Propaga el token del usuario hacia el microservicio de destino: el
        // BFF no tiene identidad propia, actua en nombre de quien llama.
        restTemplate.getInterceptors().add((request, body, execution) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtAuth.getToken().getTokenValue());
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
