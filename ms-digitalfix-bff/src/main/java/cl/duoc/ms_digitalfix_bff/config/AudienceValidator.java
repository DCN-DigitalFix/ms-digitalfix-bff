package cl.duoc.ms_digitalfix_bff.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Verifica que el token haya sido emitido PARA esta API.
 *
 * Sin esta validacion, un token valido emitido para cualquier otra aplicacion del
 * mismo tenant seria aceptado. El claim "aud" debe coincidir con el Application ID
 * URI del registro DigitalFix_api_backend.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedAudience;

    public AudienceValidator(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(expectedAudience)) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "El token no fue emitido para esta API. Audiencia esperada: " + expectedAudience,
                null);

        return OAuth2TokenValidatorResult.failure(error);
    }
}