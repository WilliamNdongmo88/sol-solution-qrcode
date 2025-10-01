package will.dev.qrcodeApp.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "code_acces", unique = true, length = 50)
    private String codeAcces;

    @Column(name = "password", length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PdfMetadata> pdfMetadataList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QrCodeMetadata> qrCodeMetadataList;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAction> actions;

    // Constructeurs
    public User() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public User(String nom, String email, String codeAcces, Role role) {
        this();
        this.nom = nom;
        this.email = email;
        this.codeAcces = codeAcces;
        this.role = role;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeAcces() {
        return codeAcces;
    }

    public void setCodeAcces(String codeAcces) {
        this.codeAcces = codeAcces;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public List<PdfMetadata> getPdfMetadataList() {
        return pdfMetadataList;
    }

    public void setPdfMetadataList(List<PdfMetadata> pdfMetadataList) {
        this.pdfMetadataList = pdfMetadataList;
    }

    public List<QrCodeMetadata> getQrCodeMetadataList() {
        return qrCodeMetadataList;
    }

    public void setQrCodeMetadataList(List<QrCodeMetadata> qrCodeMetadataList) {
        this.qrCodeMetadataList = qrCodeMetadataList;
    }

    public List<UserAction> getActions() {
        return actions;
    }

    public void setActions(List<UserAction> actions) {
        this.actions = actions;
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }

    public enum Role {
        USER, MANAGER, ADMIN
    }
}


