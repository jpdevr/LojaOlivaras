package br.com.olivaras.api.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "app_user")
public class UserEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "profile_name", nullable = false, length = 80)
    private String profileName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "google_subject", length = 255, unique = true)
    private String googleSubject;

    @Column(length = 30)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected UserEntity() {}

    public UserEntity(String profileName, String email, String passwordHash, String googleSubject,
                      String phone, LocalDate birthDate) {
        this.profileName = profileName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.googleSubject = googleSubject;
        this.phone = phone;
        this.birthDate = birthDate;
        this.role = Role.CUSTOMER;
        this.active = true;
    }

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() { updatedAt = Instant.now(); }

    public void updateProfile(String profileName, String phone) {
        if (profileName != null) this.profileName = profileName;
        if (phone != null) this.phone = phone;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.tokenVersion++;
    }

    public void anonymizeAndDeactivate(Instant deletedAt) {
        this.profileName = "Conta excluída";
        this.email = "deleted+" + id + "@deleted.invalid";
        this.passwordHash = null;
        this.googleSubject = null;
        this.phone = null;
        this.birthDate = null;
        this.active = false;
        this.deletedAt = deletedAt;
        this.tokenVersion++;
    }

    public UUID getId() { return id; }
    public String getProfileName() { return profileName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getGoogleSubject() { return googleSubject; }
    public String getPhone() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public int getTokenVersion() { return tokenVersion; }
    public Instant getDeletedAt() { return deletedAt; }
}
