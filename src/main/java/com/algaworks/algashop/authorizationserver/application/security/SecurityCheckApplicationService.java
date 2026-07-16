package com.algaworks.algashop.authorizationserver.application.security;

import java.util.UUID;

public interface SecurityCheckApplicationService {
    UUID getAuthenticatedUserId();
    boolean isAuthenticated();
    boolean isMachineAuthenticated();
    boolean canAccessOwnProfile();
}
