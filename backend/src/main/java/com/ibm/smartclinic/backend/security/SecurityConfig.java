package com.ibm.smartclinic.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/api/doctor/login",
                    "/api/patient/login",
                    "/api/auth/doctor/login",
                    "/api/auth/patient/login",
                    "/api/doctor/register",
                    "/api/patient/register"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/doctors", "/api/doctors/**").hasAnyRole("PATIENT", "DOCTOR")
                .requestMatchers("/api/doctors/**").hasRole("DOCTOR")
                .requestMatchers("/api/patient/**").hasRole("PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/appointments/*/complete").hasRole("DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/appointments/*/cancel").hasRole("DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/appointments/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/api/appointments/**").hasRole("PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/prescriptions/**").hasRole("DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/prescriptions/doctor/**").hasRole("DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/prescriptions/patient/**").hasRole("PATIENT")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
