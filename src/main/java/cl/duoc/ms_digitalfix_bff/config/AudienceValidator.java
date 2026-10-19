package cl.duoc.ms_digitalfix_bff.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Verifica que el token haya sido emitido PARA esta API.
 *
 * Sin esta validacion, un token valido emitido para cualquier otra aplicacion del
 * mismo tenant seria aceptado.
 *
 * El formato del claim "aud" depende de requestedAccessTokenVersion en el manifiesto
 * del registro de la API: con v2 llega el Application ID (GUID pelado), con v1 llega
 * el Application ID URI (api://<guid>). Se aceptan ambos para no depender de esa
 * configuracion al cambiar de tenant.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final List<String> expectedAudiences;

    public AudienceValidator(List<String> expectedAudiences) {
        this.expectedAudiences = expectedAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        boolean coincide = jwt.getAudience().stream().anyMatch(expectedAudiences::contains);

        if (coincide) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El token no fue emitido para esta API. Audiencia recibida: "
                        + jwt.getAudience() + ". Esperada alguna de: " + expectedAudiences,
                null);

        return OAuth2TokenValidatorResult.failure(error);
    }
}