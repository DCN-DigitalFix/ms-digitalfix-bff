package cl.duoc.ms_digitalfix_bff.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Devuelve lo que el BFF entendio del token recibido.
 * Util para evidenciar en la presentacion que el backend lee los claims
 * y no solo confia en que el Gateway ya valido.
 */
@RestController
@RequestMapping("/api")
public class SessionController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        String scopes = jwt.getClaimAsString("scp");

        return ResponseEntity.ok(Map.of(
                "subject", jwt.getSubject(),
                "nombre", jwt.getClaimAsString("name") != null ? jwt.getClaimAsString("name") : "",
                "usuario", jwt.getClaimAsString("preferred_username") != null
                        ? jwt.getClaimAsString("preferred_username") : "",
                "emisor", jwt.getIssuer().toString(),
                "audiencia", jwt.getAudience(),
                "roles", roles != null ? roles : List.of(),
                "scopes", scopes != null ? List.of(scopes.split(" ")) : List.of(),
                "expiraEn", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : ""));
    }
}
