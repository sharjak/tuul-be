package com.tuul.test.auth.service;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
class AuthServiceImpl implements AuthService {
    private final Clock clock;
    private final String jwtSecret;
    private final long jwtExpirationMs;

    public AuthServiceImpl(Clock clock,
                           @Value("${jwt.secret}") String jwtSecret,
                           @Value("${jwt.expiration}") long jwtExpirationMs) {
        this.clock = clock;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Override
    public Token generateJwtToken(User user) {
        Instant now = Instant.now(clock);
        Instant expiryDate = now.plusMillis(jwtExpirationMs);

        String token = Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        return Token.builder()
                .token(token)
                .expiryDate(LocalDateTime.ofInstant(expiryDate, ZoneId.systemDefault()))
                .build();
    }

    @Override
    public boolean validateJwtToken(String token) {
        try {
            createJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUserIdFromJwtToken(String token) {
        Claims claims = createJws(token).getBody();
        return claims.getSubject();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private Jws<Claims> createJws(String token) {
        var jwtToken = token.replace("Bearer ", "").trim();
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setClock(() -> Date.from(Instant.now(clock)))
                .build()
                .parseClaimsJws(jwtToken);
    }
}
