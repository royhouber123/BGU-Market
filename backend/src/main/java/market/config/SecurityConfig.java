package market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security configuration for the BGU Market application.
 * Configures security settings to allow API access while maintaining security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless for JWT
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws/notifications/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll() // Allow authentication endpoints
                .requestMatchers("/api/users/register/**").permitAll() // Allow user registration
                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                .requestMatchers("/api/**").permitAll() // Allow all API endpoints for now
                .anyRequest().authenticated() // Require authentication for other endpoints
            )
            .headers(headers -> headers.frameOptions().disable()); // Allow H2 console frames

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}