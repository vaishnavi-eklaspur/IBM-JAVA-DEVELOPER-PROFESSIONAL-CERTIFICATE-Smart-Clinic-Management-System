package com.ibm.smartclinic.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class TokenService {

    private static final String SECRET_KEY = "ibm-smart-clinic-secret-key-123456";

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .signWith(getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
