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

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Auth (público)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("POST", "/api/v1/merchants").permitAll()
                        
                        // Merchants - Requieren autenticación
                        .requestMatchers("PUT", "/api/v1/merchants/{id}/contact").authenticated()
                        .requestMatchers("PUT", "/api/v1/merchants/{id}/bank-account").authenticated()
                        .requestMatchers("PATCH", "/api/v1/merchants/{id}/activate").hasRole("ADMIN")
                        .requestMatchers("PATCH", "/api/v1/merchants/{id}/deactivate").hasRole("ADMIN")
                        .requestMatchers("GET", "/api/v1/merchants/{id}").authenticated()
                        
                        // Otros
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

