package will.dev.qrcodeApp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_actions")
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_code_id")
    private QrCodeMetadata qrCodeMetadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action", nullable = false, length = 50)
    private TypeAction typeAction;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_action")
    private LocalDateTime dateAction;

    // Constructeurs
    public UserAction() {
        this.dateAction = LocalDateTime.now();
    }

    public UserAction(User utilisateur, TypeAction typeAction, String description) {
        this();
        this.utilisateur = utilisateur;
        this.typeAction = typeAction;
        this.description = description;
    }

    public UserAction(User utilisateur, QrCodeMetadata qrCodeMetadata, TypeAction typeAction, String description) {
        this(utilisateur, typeAction, description);
        this.qrCodeMetadata = qrCodeMetadata;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
    }

    public QrCodeMetadata getQrCode() {
        return qrCodeMetadata;
    }

    public void setQrCode(QrCodeMetadata qrCode) {
        this.qrCodeMetadata = qrCode;
    }

    public TypeAction getTypeAction() {
        return typeAction;
    }

    public void setTypeAction(TypeAction typeAction) {
        this.typeAction = typeAction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public enum TypeAction {
        CREATION_QR,
        CREATION_COMPTE,
        GENERATION_QR,
        UPLOAD_PDF,
        SUPPRESSION_PDF,
        CONNEXION,
        MODIFICATION_PROFIL,
        GENERATION_CODE_ACCES,
        SUPPRESSION_QR,
        ACTIVATION_COMPTE,
        DESACTIVATION_COMPTE,
        CHANGEMENT_ROLE
    }
}

