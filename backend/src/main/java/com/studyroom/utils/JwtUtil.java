package com.studyroom.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    @Value("${jwt.refresh-expire}")
    private long refreshExpire;

    public String generateToken(String subject) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + refreshExpire);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubject(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public Date getIssuedAt(String token) {
        Claims claims = parseToken(token);
        return claims.getIssuedAt();
    }

    public Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }
}