package com.algaworks.algashop.authorizationserver.infrastructure.security.check;

import com.algaworks.algashop.authorizationserver.application.security.SecurityChecks;
import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service("securityChecks")
@Slf4j
public class OAuth2SecurityChecksImpl implements SecurityChecks {

    private static final String SCOPE_USERS_WRITE = "SCOPE_users:write";
    private static final String ROLE_MANAGER = "ROLE_" + AuthUserType.MANAGER.name();

    @Override
    public UUID getAuthenticatedUserId() {
        if (isMachineAuthenticated()) {
            throw new AccessDeniedException("Machine users do not have user ID");
        }
        Jwt jwt = getJwt();

        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalAccessError e) {
            log.error("Invalid user ID in JWT subject: {}", jwt.getSubject(), e);
            throw new AccessDeniedException("Invalid user ID in JWT subject");
        }
    }

    @Override
    public boolean isAuthenticated() {
        try {
            return getAuthentication().isAuthenticated();
        } catch (IllegalStateException e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isMachineAuthenticated() {
        Jwt jwt;
        try {
            jwt = getJwt();
        } catch (IllegalStateException e) {
            log.debug(e.getMessage(), e);
            return false;
        }
        return jwt.getAudience().contains(jwt.getSubject());
    }

    @Override
    public boolean canAccessOwnProfile() {
        return this.isAuthenticated() && !this.isMachineAuthenticated();
    }

    @Override
    public boolean canRegisterUserOfType(AuthUserType authUserType) {
        if (!isAuthenticated()) {
            return false;
        }
        if (!hasAuthority(SCOPE_USERS_WRITE)) {
            return false;
        }
        if (AuthUserType.CUSTOMER.equals(authUserType)) {
            return isMachineAuthenticated();
        }
        if (hasAuthority(ROLE_MANAGER)) {
            return AuthUserType.MANAGER.equals(authUserType) || AuthUserType.OPERATOR.equals(authUserType);
        }
        return false;
    }

    @Override
    public boolean canEditUser(AuthUserType editType, UUID editUserId) {
        if (isMachineAuthenticated()) {
            return false;
        }
        try {
            if (getAuthenticatedUserId().equals(editUserId)) {
                return true;
            }
        } catch (AccessDeniedException _) {
            return false;
        }
        if (hasAuthority(ROLE_MANAGER)) {
            return AuthUserType.MANAGER.equals(editType) || AuthUserType.OPERATOR.equals(editType);
        }
        return false;
    }

    @Override
    public boolean canChangeUserType(AuthUserType currentType, AuthUserType newType) {
        if (currentType.equals(newType)) {
            return true;
        }
        if (hasAuthority(ROLE_MANAGER)) {
            if (AuthUserType.MANAGER.equals(currentType) && AuthUserType.OPERATOR.equals(newType)) {
                return true;
            }
            if (AuthUserType.OPERATOR.equals(currentType) && AuthUserType.MANAGER.equals(newType)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAuthority(String rawAuthority) {
        Authentication authentication;
        try {
            authentication = getAuthentication();
        } catch (IllegalStateException e) {
            log.debug(e.getMessage(), e);
            return false;
        }
        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), rawAuthority));
    }

    private Jwt getJwt() {
        Authentication authentication = getAuthentication();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        throw new IllegalStateException("Authentication principal is not a JWT");
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }
        return authentication;
    }
}
