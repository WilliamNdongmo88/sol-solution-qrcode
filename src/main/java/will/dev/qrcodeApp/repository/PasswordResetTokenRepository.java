package will.dev.qrcodeApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import will.dev.qrcodeApp.entity.PasswordResetToken;
import will.dev.qrcodeApp.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    
    @Query("SELECT p FROM PasswordResetToken p WHERE p.token = :token AND p.used = false AND p.expiryDate > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    void deleteByUser(User user);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    boolean existsByUserAndUsedFalse(User user);
}

