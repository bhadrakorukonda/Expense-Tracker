package com.expense.tracker.security;

import com.expense.tracker.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that intercepts requests and validates JWT tokens
 * Runs once per request to extract and validate JWT from Authorization header
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    /**
     * Filter incoming requests to validate JWT tokens
     * Extracts token from Authorization header, validates it, and sets authentication
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Extract JWT token from Authorization header
            String jwt = extractJwtFromRequest(request);
            
            // If token exists and no authentication is set yet
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Extract username from token
                String username = jwtTokenUtil.extractUsername(jwt);
                
                // Load user details
                UserDetails userDetails = userService.loadUserByUsername(username);
                
                // Validate token
                if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                            );
                    
                    // Set authentication details
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT authentication successful for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer <token>"
     * 
     * @param request HTTP request
     * @return JWT token string or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if Authorization header exists and starts with "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }
}
