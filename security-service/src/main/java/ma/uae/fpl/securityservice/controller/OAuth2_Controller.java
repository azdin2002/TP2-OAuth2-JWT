package ma.uae.fpl.securityservice.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class OAuth2_Controller {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshTokenDecoder;

    @Value("${jwt.access-token-minutes}")
    private long accessTokenMinutes;

    @Value("${jwt.refresh-token-minutes}")
    private long refreshTokenMinutes;

    @Value("${jwt.issuer}")
    private String issuer;

    public OAuth2_Controller(AuthenticationManager authenticationManager,
                             UserDetailsService userDetailsService,
                             JwtEncoder jwtEncoder,
                             @Qualifier("refreshTokenDecoder") JwtDecoder refreshTokenDecoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenDecoder = refreshTokenDecoder;
    }

    /**
     * POST /login  (parametres : username, password)
     * Authentifie l'utilisateur et renvoie l'Access Token et le Refresh Token.
     */
    @PostMapping({"/login", "/Login"})
    public ResponseEntity<Map<String, String>> login(@RequestParam String username,
                                                     @RequestParam String password) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            return error("Identifiants invalides");
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();

        String accessToken = generateToken(authentication.getName(), roles, "access",
                Duration.ofMinutes(accessTokenMinutes));
        String refreshToken = generateToken(authentication.getName(), roles, "refresh",
                Duration.ofMinutes(refreshTokenMinutes));

        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken));
    }

    /**
     * POST /refreshToken  (parametre : refreshToken)
     * Renouvelle l'Access Token a partir d'un Refresh Token valide.
     */
    @PostMapping({"/refreshToken", "/RefreToken"})
    public ResponseEntity<Map<String, String>> refreshToken(@RequestParam String refreshToken) {
        Jwt jwt;
        try {
            jwt = refreshTokenDecoder.decode(refreshToken);
        } catch (Exception e) {
            return error("Refresh token invalide ou expire");
        }

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(jwt.getSubject());
        } catch (UsernameNotFoundException e) {
            return error("Utilisateur inconnu");
        }

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();

        String newAccessToken = generateToken(user.getUsername(), roles, "access",
                Duration.ofMinutes(accessTokenMinutes));

        return ResponseEntity.ok(Map.of(
                "access_token", newAccessToken,
                "refresh_token", refreshToken));
    }

    /** GET /profile : endpoint protege pour verifier qu'un Access Token est accepte. */
    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList());
    }

    private String generateToken(String username, List<String> roles, String type, Duration validity) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(validity))
                .claim("roles", roles)
                .claim("type", type)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private ResponseEntity<Map<String, String>> error(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", message));
    }
}
