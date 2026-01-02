package com.ibm.smartclinic.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/doctor/login", "/api/patient/login", "/api/doctor/register", "/api/patient/register").permitAll()
                .requestMatchers("/api/doctors/**").hasRole("DOCTOR")
                .requestMatchers("/api/patient/**").hasRole("PATIENT")
                .requestMatchers(HttpMethod.POST, "/api/appointments/*/complete").hasRole("DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/appointments/*/cancel").hasRole("DOCTOR")
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
