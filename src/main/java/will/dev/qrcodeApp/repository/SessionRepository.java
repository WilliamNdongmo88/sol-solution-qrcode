package will.dev.qrcodeApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import will.dev.qrcodeApp.entity.Session;
import will.dev.qrcodeApp.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByToken(String token);

    List<Session> findByUtilisateur(User utilisateur);

    List<Session> findByUtilisateurId(Long utilisateurId);

    @Query("SELECT s FROM Session s WHERE s.dateExpiration < :now")
    List<Session> findExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.dateExpiration < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.utilisateur.id = :utilisateurId")
    void deleteByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    @Query("SELECT s FROM Session s WHERE s.token = :token AND s.dateExpiration > :now")
    Optional<Session> findValidSessionByToken(@Param("token") String token, @Param("now") LocalDateTime now);
}

