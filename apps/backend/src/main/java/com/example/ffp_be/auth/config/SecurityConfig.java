package com.example.ffp_be.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint entryPoint;
    private final CustomAccessDeniedHandler deniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/login", "/auth/join", "/auth/refresh", "/error", "/actuator/health",
                    "/api/auth/login", "/api/auth/join", "/api/auth/refresh",
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
                    "/swagger-ui.html", "/webjars/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/posts",
                    "/posts/recent",
                    "/posts/search",
                    "/posts/*"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/news/latest",
                    "/matches",
                    "/matches/*",
                    "/matches/**",
                    "/games/*",
                    "/games/**",
                    "/leagues",
                    "/leagues/*",
                    "/leagues/**",
                    "/teams",
                    "/teams/*",
                    "/teams/**",
                    "/players",
                    "/players/*",
                    "/players/**",
                    "/comments/post/*",
                    "/comments/post/**",
                    "/clip/*",
                    "/clip/game/*",
                    "/clip/game/**",
                    "/clip/user/*",
                    "/users/by-nickname/*",
                    "/users/by-id/*",
                    "/users/search",
                    "/users/availability"
                ).permitAll()
                .anyRequest().hasRole("USER")
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://www.aim-on.store", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/v3/api-docs/**", config);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
