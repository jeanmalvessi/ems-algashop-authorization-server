package com.algaworks.algashop.authorizationserver.application.security;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;

import java.util.UUID;

public interface SecurityCheckApplicationService {
    UUID getAuthenticatedUserId();
    boolean isAuthenticated();
    boolean isMachineAuthenticated();
    boolean canAccessOwnProfile();
    boolean canRegisterUserOfType(AuthUserType authUserType);
    boolean canEditUser(AuthUserType editType, UUID editUserId);
    boolean canChangeUserType(AuthUserType currentType, AuthUserType newType);
}
