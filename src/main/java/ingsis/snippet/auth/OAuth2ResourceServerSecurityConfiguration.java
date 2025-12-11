package ingsis.snippet.auth;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class OAuth2ResourceServerSecurityConfiguration {

  private final String audience;
  private final String issuer;

  public OAuth2ResourceServerSecurityConfiguration(
      @Value("${auth0.audience}") String audience,
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
    this.audience = audience;
    this.issuer = issuer;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/ping",
                        "/actuator/**",
                        "/swagger-ui",
                        "/swagger-ui/*",
                        "/v3/api-docs",
                        "/v3/api-docs/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/snippets/**")
                    .hasAuthority("SCOPE_read:snippets")
                    .requestMatchers(HttpMethod.POST, "/snippets/**")
                    .hasAuthority("SCOPE_write:snippets")
                    .requestMatchers(HttpMethod.DELETE, "/snippets/**")
                    .hasAuthority("SCOPE_write:snippets")
                    .requestMatchers(HttpMethod.PUT, "/snippets/**")
                    .hasAuthority("SCOPE_write:snippets")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
        .cors(withDefaults())
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder dec = NimbusJwtDecoder.withIssuerLocation(issuer).build();
    OAuth2TokenValidator<Jwt> aud = new AudienceValidator(audience);
    OAuth2TokenValidator<Jwt> iss = JwtValidators.createDefaultWithIssuer(issuer);
    dec.setJwtValidator(new DelegatingOAuth2TokenValidator<>(iss, aud));
    return dec;
  }

  // CorsConfig
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("*"));
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", cfg);

    return src;
  }
}
