package will.dev.qrcodeApp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @Column(name = "token", unique = true, nullable = false, length = 255)
    private String token;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    // Constructeurs
    public Session() {
        this.dateCreation = LocalDateTime.now();
    }

    public Session(User utilisateur, String token, LocalDateTime dateExpiration) {
        this();
        this.utilisateur = utilisateur;
        this.token = token;
        this.dateExpiration = dateExpiration;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.dateExpiration);
    }
}

