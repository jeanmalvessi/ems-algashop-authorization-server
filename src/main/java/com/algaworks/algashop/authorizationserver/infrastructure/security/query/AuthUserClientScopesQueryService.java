package com.algaworks.algashop.authorizationserver.infrastructure.security.query;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;

import java.util.Set;

public interface AuthUserClientScopesQueryService {
    Set<String> findAllowedScopesByRoleAndClientId(AuthUserType role, String clientId);
}
