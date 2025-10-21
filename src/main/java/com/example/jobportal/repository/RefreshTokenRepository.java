package com.example.jobportal.repository;

import com.example.jobportal.model.entity.RefreshToken;
import com.example.jobportal.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUser(User user);
    List<RefreshToken> findByUserAndIsRevokedFalse(User user);

    @Query("SELECT t FROM RefreshToken t WHERE t.user = :user AND t.isRevoked = false AND t.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM RefreshToken t WHERE t.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    void deleteByUserId(Long id);
}
