package com.panacea.backend.config;

import com.panacea.backend.security.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    public UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(
                new BCryptPasswordEncoder(12)
        );

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // CORS
                .cors(cors -> {})

                // JWT-based API — CSRF disabled
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(request -> request

                        // =========================================
                        // PUBLIC AUTH ENDPOINTS
                        // =========================================
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()
                        .requestMatchers("/api/health").permitAll()
                        // =========================================
                        // PUBLIC SWAGGER / API DOCS
                        // =========================================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =========================================
                        // BR CARD
                        // =========================================

                        // Admin BR Card endpoints
                        .requestMatchers(
                                "/api/br-cards/admin/**"
                        ).hasRole("ADMIN")

                        // QR verification is an admin operation
                        .requestMatchers(
                                "/api/br-cards/verify"
                        ).hasRole("ADMIN")

                        // User BR Card endpoints
                        .requestMatchers(
                                "/api/br-cards/**"
                        ).authenticated()

                        // =========================================
                        // EXISTING APPLICATION ROUTES
                        // =========================================
                        .requestMatchers(
                                "/apply/**"
                        ).authenticated()

                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/guest/**"
                        ).hasRole("GUEST")

                        .requestMatchers(
                                "/owner/**"
                        ).hasRole("OWNER")

                        // =========================================
                        // OPTIONS — CORS PREFLIGHT
                        // =========================================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================
                        .anyRequest().authenticated()
                )

                // Authentication failure handling
                .exceptionHandling(
                        exception -> exception
                                .authenticationEntryPoint(
                                        customAuthenticationEntryPoint
                                )
                )

                // Stateless JWT authentication
                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                // JWT filter runs before Spring's username/password filter
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }
}