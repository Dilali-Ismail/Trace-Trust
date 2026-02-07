package org.usermanagement.traceandtrust.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.dto.AuthResponse;
import org.usermanagement.traceandtrust.dto.LoginRequest;
import org.usermanagement.traceandtrust.entity.RefreshToken;
import org.usermanagement.traceandtrust.entity.User;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest){

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userDetailsService.getUserByEmail(loginRequest.getEmail());
            
            if (!user.isActive()) {
                throw new org.springframework.security.authentication.DisabledException("Compte utilisateur désactivé");
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateAcessToken(userDetails, user.getRole().name());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(900L)
                    .role(user.getRole().name())
                    .email(user.getEmail())
                    .build();

        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }

    public AuthResponse refreshAccessToken(String refreshTokenValue){

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide"));
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        if (!user.isActive()) {
            throw new RuntimeException("Compte utilisateur désactivé");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateAcessToken(userDetails, user.getRole().name());

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(900L)
                .role(user.getRole().name())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        log.info("User logging out.");
        refreshTokenService.revokeRefreshToken(refreshTokenValue);
    }

    @Transactional
    public void logoutAll(String email) {
        User user = userDetailsService.getUserByEmail(email);
        refreshTokenService.revokeAllUserTokens(user);
    }


}
