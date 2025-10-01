package will.dev.qrcodeApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.repository.UserActionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionRepository userActionRepository;

    /**
     * Enregistrer une action utilisateur
     */
    public UserAction logAction(User utilisateur, UserAction.TypeAction typeAction, String description) {
        UserAction action = new UserAction(utilisateur, typeAction, description);
        return userActionRepository.save(action);
    }

    /**
     * Obtenir toutes les actions d'un utilisateur
     */
    public List<UserAction> getUserActions(Long utilisateurId) {
        return userActionRepository.findByUtilisateurIdOrderByDateActionDesc(utilisateurId);
    }

    /**
     * Obtenir toutes les actions (pour les admins)
     */
    public List<UserAction> getAllActions() {
        return userActionRepository.findAllByOrderByDateActionDesc();
    }

    /**
     * Obtenir les actions par type
     */
    public List<UserAction> getActionsByType(UserAction.TypeAction typeAction) {
        return userActionRepository.findByTypeAction(typeAction);
    }

    /**
     * Obtenir les actions dans une période donnée
     */
    public List<UserAction> getActionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return userActionRepository.findByDateActionBetween(startDate, endDate);
    }

    /**
     * Compter les actions d'un utilisateur par type
     */
    public long countUserActionsByType(Long utilisateurId, UserAction.TypeAction typeAction) {
        return userActionRepository.countByUtilisateurIdAndTypeAction(utilisateurId, typeAction);
    }
}


