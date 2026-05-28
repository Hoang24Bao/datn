package com.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.info("=== Processing request: {} ===", requestURI);

        // Bỏ qua các request public
        if (requestURI.startsWith("/css/") || requestURI.startsWith("/js/") ||
                requestURI.startsWith("/img/") || requestURI.startsWith("/lib/") ||
                requestURI.equals("/") || requestURI.equals("/home") ||
                requestURI.equals("/login") || requestURI.equals("/signup") ||
                requestURI.startsWith("/api/auth/")) {
            logger.info("Public request, skipping auth: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            logger.info("JWT parsed: {}", jwt != null ? "YES (first 30 chars: " + jwt.substring(0, Math.min(30, jwt.length())) + "...)" : "NO");

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                String role = jwtUtils.getRoleFromJwtToken(jwt);

                logger.info("Token valid! Username: {}, Role: {}", username, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username, null,
                                Collections.singletonList(new SimpleGrantedAuthority(role))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authentication set in SecurityContext");
            } else {
                logger.warn("Token invalid or null for request: {}", requestURI);
            }
        } catch (Exception e) {
            logger.error("Error processing auth token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // 1. Check Authorization header
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            logger.debug("Found token in Authorization header");
            return headerAuth.substring(7);
        }

        // 2. Check cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    logger.debug("Found token in Cookie");
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}