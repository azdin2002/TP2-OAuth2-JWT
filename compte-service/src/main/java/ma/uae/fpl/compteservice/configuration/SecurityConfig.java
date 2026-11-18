package ma.uae.fpl.compteservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${rsa.public-key}")
    private Resource publicKeyResource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ajout, modification, suppression, liste complete : ADMIN
                        .requestMatchers(HttpMethod.POST, "/comptes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/comptes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/comptes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/comptes").hasRole("ADMIN")
                        // consultation par ID, solde, credit, debit : tout utilisateur authentifie
                        .requestMatchers(HttpMethod.GET, "/comptes/*", "/comptes/*/solde").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comptes/*/credit", "/comptes/*/debit").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /** Verifie la signature avec la cle publique du service de securite (aucune cle privee ici). */
    @Bean
    public JwtDecoder jwtDecoder() throws IOException {
        RSAPublicKey publicKey;
        try (InputStream in = publicKeyResource.getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(in);
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), accessTokenOnly()));
        return decoder;
    }

    /** Un Refresh Token ne doit jamais ouvrir l'API. */
    private OAuth2TokenValidator<Jwt> accessTokenOnly() {
        return jwt -> "access".equals(jwt.getClaimAsString("type"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Un Access Token est requis", null));
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
