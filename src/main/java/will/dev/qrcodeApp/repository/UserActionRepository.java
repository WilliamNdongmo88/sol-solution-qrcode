package will.dev.qrcodeApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findByUtilisateur(User utilisateur);

    List<UserAction> findByUtilisateurId(Long utilisateurId);

    List<UserAction> findByUtilisateurIdOrderByDateActionDesc(Long utilisateurId);

    List<UserAction> findByTypeAction(UserAction.TypeAction typeAction);

    @Query("SELECT ua FROM UserAction ua WHERE ua.dateAction BETWEEN :startDate AND :endDate")
    List<UserAction> findByDateActionBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    List<UserAction> findAllByOrderByDateActionDesc();

    @Query("SELECT COUNT(ua) FROM UserAction ua WHERE ua.utilisateur.id = :utilisateurId AND ua.typeAction = :typeAction")
    long countByUtilisateurIdAndTypeAction(@Param("utilisateurId") Long utilisateurId, 
                                         @Param("typeAction") UserAction.TypeAction typeAction);
}


