package will.dev.qrcodeApp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import will.dev.qrcodeApp.entity.User;
import will.dev.qrcodeApp.entity.UserAction;
import will.dev.qrcodeApp.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserActionService userActionService;
    private final PasswordEncoder passwordEncoder;
    private final BrevoService brevoService;

    /**
     * Créer un nouvel utilisateur
     */
    @Transactional
    public User createUser(String nom, String email, String password, User.Role role) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        User user = new User();
        user.setNom(nom);
        user.setEmail(email);
        user.setRole(role);
        user.setActif(true);

        if (role == User.Role.USER) {
            String codeAcces = generateUniqueAccessCode();
            user.setCodeAcces(codeAcces);
            user.setPassword(null); // Les utilisateurs normaux se connectent avec le code d'accès
            // Envoyer l'email avec le code d'accès
            brevoService.sendWelcomeEmail(user, codeAcces); // Envoyer le code d'accès au lieu du mot de passe temporaire
        } else { // ADMIN ou MANAGER
            String tempPassword = (password != null && !password.isEmpty()) ? password : generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setCodeAcces(null);
            // Envoyer l'email avec le mot de passe temporaire
            brevoService.sendWelcomeEmail(user, tempPassword); // Envoyer le mot de passe temporaire
        }

        user = userRepository.save(user);

        // Enregistrer l'action de création
        userActionService.logAction(user,
                UserAction.TypeAction.CREATION_COMPTE,
                null,
                null,
                false,
                "Compte créé pour: " + user.getEmail() + " avec le rôle: " + user.getRole());

        return user;
    }

    /**
     * Modifier le rôle d'un utilisateur
     */
    @Transactional
    public User changeUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        User.Role oldRole = user.getRole();
        user.setRole(newRole);

        // Si on promeut en admin/manager et qu'il n'a pas de mot de passe, en générer un
        if ((newRole == User.Role.ADMIN || newRole == User.Role.MANAGER) && user.getPassword() == null) {
            String tempPassword = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            brevoService.sendRoleChangeNotification(user, oldRole.name(), newRole.name(), tempPassword); // Envoyer le nouveau mot de passe
        } else {
            brevoService.sendRoleChangeNotification(user, oldRole.name(), newRole.name(), null); // Pas de nouveau mot de passe à envoyer
        }

        user = userRepository.save(user);

        // Enregistrer l'action
        userActionService.logAction(user, UserAction.TypeAction.CHANGEMENT_ROLE,null,null,false,
                "Changement de rôle de " + oldRole + " vers " + newRole + " pour l'utilisateur " + user.getEmail());

        return user;
    }

    /**
     * Activer/désactiver un utilisateur
     */
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        boolean oldStatus = user.getActif();
        user.setActif(!oldStatus);
        user = userRepository.save(user);

        // Enregistrer l'action
        UserAction.TypeAction actionType = user.getActif() ?
                UserAction.TypeAction.ACTIVATION_COMPTE : UserAction.TypeAction.DESACTIVATION_COMPTE;

        userActionService.logAction(user, actionType,null,null, false,
                (user.getActif() ? "Activation" : "Désactivation") + " du compte " + user.getEmail());

        brevoService.sendStatusChangeNotification(user, user.getActif());

        return user;
    }

    /**
     * Obtenir tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Obtenir un utilisateur par ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Obtenir un utilisateur par email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Mettre à jour le profil utilisateur
     */
    @Transactional
    public User updateUserProfile(Long userId, String nom, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si le nouvel email n'est pas déjà utilisé par un autre utilisateur
        if (!user.getEmail().equals(email) && userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
        }

        user.setNom(nom);
        user.setEmail(email);
        user = userRepository.save(user);

        // Enregistrer l'action
        userActionService.logAction(user, UserAction.TypeAction.MODIFICATION_PROFIL,null,null,
                false,
                "Modification du profil utilisateur");

        return user;
    }

    /**
     * Générer un code d'accès unique
     */
    private String generateUniqueAccessCode() {
        String codeAcces;
        do {
            codeAcces = generateAccessCode();
        } while (userRepository.findByCodeAcces(codeAcces).isPresent());

        return codeAcces;
    }

    /**
     * Générer un code d'accès aléatoire
     */
    private String generateAccessCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        // Générer un code de 8 caractères alphanumériques
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * Générer un mot de passe temporaire
     */
    private String generateTemporaryPassword() {
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }


    @Transactional
    public String regenerateAccessCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Génère un nouveau code unique
        String newCode = generateUniqueAccessCode();

        // Met à jour l'utilisateur
        user.setCodeAcces(newCode);
        userRepository.save(user);
        Boolean isReset = true;
        brevoService.sendWelcomeEmail(user, newCode, isReset);

        return "Régénération d'un nouveau code d'accès réussi : " + newCode;
    }
}

