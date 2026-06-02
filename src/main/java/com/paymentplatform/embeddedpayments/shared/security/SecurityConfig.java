package com.paymentplatform.embeddedpayments.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Public checkout (sin autenticación requerida)
                        .requestMatchers("/checkout/**").permitAll()

                        // Auth (público)
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh", "/api/v1/auth/token").permitAll()
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").authenticated()
                        .requestMatchers("/api/v1/merchants").permitAll()
                        
                        // Merchants - Requieren autenticación
                        .requestMatchers("/api/v1/merchants/*/contact").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/v1/merchants/*/bank-account").hasAnyRole("ADMIN", "MERCHANT")
                        .requestMatchers("/api/v1/merchants/*/activate").hasRole("ADMIN")
                        .requestMatchers("/api/v1/merchants/*/deactivate").hasRole("ADMIN")
                        .requestMatchers("/api/v1/merchants/*").hasAnyRole("ADMIN", "MERCHANT")

                        // Administración de pagos (JWT de merchant/admin)
                        .requestMatchers("/api/v1/admin/payments/**").hasAnyRole("ADMIN", "MERCHANT")

                        // Embedded checkout / processing
                        .requestMatchers("/api/v1/payments/**").hasRole("API_CLIENT")
                        .requestMatchers("/api/v1/transactions/**").hasAnyRole("API_CLIENT", "MERCHANT", "USER")
                        .requestMatchers("/api/v1/refunds/**").hasAnyRole("API_CLIENT", "MERCHANT", "USER")

                        // Otros
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, ApiKeyAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.onrender.com",
                "https://embedded-payments-1.onrender.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-API-Key"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
