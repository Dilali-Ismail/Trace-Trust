package org.usermanagement.traceandtrust.service;


import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.usermanagement.traceandtrust.entity.RefreshToken;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public RefreshToken createRefreshToken(User user) {
       RefreshToken refreshToken = new RefreshToken();
       refreshToken.setUser(user);
       refreshToken.setToken(UUID.randomUUID().toString());
       refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
       refreshToken.setCreatedAt(Instant.now());
       refreshToken.setRevoked(false);
       return refreshTokenRepository.save(refreshToken);

    }


    public Optional<RefreshToken> findByToken(String token) {
          return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if (token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        if(token.isRevoked()){
            throw new RuntimeException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        return token;
    }

    public RefreshToken  rotateRefreshToken(RefreshToken oldToken){
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

       return  createRefreshToken(oldToken.getUser());
    }

    public void  revokeRefreshToken(String token){

        refreshTokenRepository.findByToken(token).ifPresent(refreshToken ->{
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        } );
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteRevokedAndExpiredTokens();
    }

}
