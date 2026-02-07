package org.usermanagement.traceandtrust.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.usermanagement.traceandtrust.entity.RefreshToken;
import org.usermanagement.traceandtrust.entity.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

   Optional<RefreshToken> findByToken(String token);
   void deleteByUser(User user);

   @Modifying
   @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiryDate < current_timestamp ")
   void deleteRevokedAndExpiredTokens();

}
