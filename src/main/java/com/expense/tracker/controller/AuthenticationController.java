package com.expense.tracker.controller;

import com.expense.tracker.dto.LoginRequest;
import com.expense.tracker.dto.LoginResponse;
import com.expense.tracker.model.User;
import com.expense.tracker.security.JwtTokenUtil;
import com.expense.tracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for user login and JWT token generation
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and JWT token management")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Authenticate user and generate JWT token
     * 
     * @param loginRequest login credentials (email and password)
     * @return JWT token and user information
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("POST /api/v1/auth/login - Login attempt for email: {}", loginRequest.getEmail());
        
        try {
            // Authenticate user with Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            
            // Load user details
            UserDetails userDetails = userService.loadUserByUsername(loginRequest.getEmail());
            
            // Get full user entity for additional info
            User user = userService.findByEmail(loginRequest.getEmail());
            
            // Generate JWT token
            String token = jwtTokenUtil.generateToken(userDetails, user.getId());
            
            log.info("Login successful for user: {}", loginRequest.getEmail());
            
            // Return token and user info
            return ResponseEntity.ok(LoginResponse.of(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getName()
            ));
            
        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {} - Invalid credentials", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
