package com.example.ffp_be.auth.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token-validity-in-seconds}")
    private long tokenValidityMs;

    private final UserDetailsService userDetailsService;
    private final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private SecretKey key;

    @PostConstruct
    protected void init() {
        if (secret == null || secret.length() < 32) {
            secret = String.format("%-32s", secret == null ? "" : secret).replace(' ', '0');
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String username, List<String> roles) {
        List<String> rolesWithPrefix = new ArrayList<>();
        for (String role : roles) {
            if (role.startsWith("ROLE_")) {
                rolesWithPrefix.add(role);
            } else {
                rolesWithPrefix.add("ROLE_" + role);
            }
        }
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", rolesWithPrefix);

        Date now = new Date();
        Date exp = new Date(now.getTime() + tokenValidityMs);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(
            userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            log.info("잘못된 Jwt 토큰입니다");
        } catch (ExpiredJwtException e) {
            log.info("만료된 Jwt 토큰입니다");
        } catch (UnsupportedJwtException e) {
            log.info("지원하지 않는 Jwt 토큰입니다");
        }
        return false;
    }
}
