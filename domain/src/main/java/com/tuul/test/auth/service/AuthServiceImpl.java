package com.tuul.test.auth.service;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiryDate = now.plus(jwtExpirationMs, ChronoUnit.MILLIS);

        String token = Jwts.builder()
                .setSubject(user.getId())
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        return Token.builder()
                .token(token)
                .expiryDate(expiryDate)
                .build();
    }

    @Override
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
