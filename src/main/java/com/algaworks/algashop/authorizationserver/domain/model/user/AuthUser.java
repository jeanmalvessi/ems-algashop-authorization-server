package com.algaworks.algashop.authorizationserver.domain.model.user;

import com.algaworks.algashop.authorizationserver.domain.model.AbstractAuditableAggregateRoot;
import com.algaworks.algashop.authorizationserver.domain.model.DomainException;
import com.algaworks.algashop.authorizationserver.domain.model.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_user")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthUser extends AbstractAuditableAggregateRoot<AuthUser> {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private String email;
    private String password;
    private String name;
    private boolean enabled;
    private boolean emailVerified;
    private String verificationToken;
    private OffsetDateTime verificationTokenExpirationDate;

    @Enumerated(EnumType.STRING)
    private AuthUserType type;

    public static AuthUser brandNew(String email, String name, AuthUserType type, AuthUserPasswordManager passwordManager) {
        AuthUser user = new AuthUser();
        user.setId(IdGenerator.generateTimeBasedUUID());
        user.setEmail(email);
        user.setName(name);
        user.setType(type);
        user.setPassword(passwordManager.encrypt(passwordManager.generate()));
        user.setEnabled(true);
        user.setEmailVerified(false);
        return user;
    }

    public String generateVerificationToken(Duration expiresIn, VerificationTokenHasher hasher) {
        String plainToken = hasher.generate();
        this.verificationToken = hasher.hash(plainToken);
        this.verificationTokenExpirationDate = OffsetDateTime.now().plus(expiresIn);
        return plainToken;
    }

    public void changePasswordWithToken(String plainToken, String plainPassword, AuthUserPasswordManager passwordManager, VerificationTokenHasher tokenHasher) {
        verifyToken(plainToken, tokenHasher);
        setPassword(passwordManager.encrypt(plainPassword));
        cleanVerificationToken();
        if (!isEmailVerified()) {
            setEmailVerified(true);
        }
    }

    public boolean isDisabled() {
        return !isEmailVerified() || !isEnabled();
    }

    public void anonymize() {
        this.setName("Anonymized User");
        this.setEmail("anonymized-" + this.id + "@deleted.local");
        this.setEnabled(false);
    }

    public void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setType(AuthUserType type) {
        Objects.requireNonNull(type);
        if (AuthUserType.CUSTOMER.equals(this.type)) {
            throw new DomainException("Cannot change type of a CUSTOMER user");
        }
        this.type = type;
    }

    private void verifyToken(String plainToken, VerificationTokenHasher tokenHasher) {
        if (!tokenHasher.isEqual(this.verificationToken, plainToken)) {
            throw new IllegalArgumentException("Invalid token");
        }
        if (isTokenExpired()) {
            throw new IllegalStateException("Token has expired");
        }
    }

    private boolean isTokenExpired() {
        if (verificationTokenExpirationDate == null) {
            return true;
        }
        return OffsetDateTime.now().isAfter(verificationTokenExpirationDate);
    }

    private void cleanVerificationToken() {
        this.verificationToken = null;
        this.verificationTokenExpirationDate = null;
    }

    private void setPassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException();
        }
        this.password = password;
    }

    private void setId(UUID id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    private void setEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException();
        }
        this.email = email;
    }

    private void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
