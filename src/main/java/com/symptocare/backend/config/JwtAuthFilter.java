// src/main/java/com/symptocare/backend/config/JwtAuthFilter.java
package com.symptocare.backend.config;

import com.symptocare.backend.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract token from Authorization header
        String token = extractToken(request);

        // 2. If no token found, just continue — security config will block if needed
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Try to validate and set authentication
        try {
            String email = jwtService.extractEmail(token);

            // Only set auth if email extracted and no existing auth in context
            if (StringUtils.hasText(email) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(token, email)) {

                    // Build authentication token with no roles (stateless)
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email,   // principal — this becomes auth.getName() in controllers
                                    null,    // credentials — not needed after JWT validation
                                    List.of() // authorities — add roles here later if needed
                            );

                    // Attach request details (IP, session) to the auth token
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set in security context — marks request as authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {}", email);
                } else {
                    log.warn("Invalid or expired JWT token for request: {}", request.getRequestURI());
                }
            }

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;

        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Malformed token");
            return;

        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
            return;
        }

        // 4. Continue filter chain
        filterChain.doFilter(request, response);
    }

    // Extract Bearer token from Authorization header
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    // Send clean JSON error response instead of default Spring error page
    private void sendErrorResponse(HttpServletResponse response,
                                   int status,
                                   String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
                String.format("{\"error\": \"%s\", \"status\": %d}", message, status)
        );
    }

    // Skip JWT filter for public endpoints
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") ||
               path.equals("/") ||
               path.startsWith("/ws/");
    }
}
