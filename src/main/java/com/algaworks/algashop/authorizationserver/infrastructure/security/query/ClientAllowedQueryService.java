package com.algaworks.algashop.authorizationserver.infrastructure.security.query;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;

import java.util.Set;

public interface ClientAllowedQueryService {
    Set<String> findByRole(AuthUserType role);
}
