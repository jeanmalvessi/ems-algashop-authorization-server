package com.algaworks.algashop.authorizationserver.infrastructure.security.token;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;
import com.algaworks.algashop.authorizationserver.infrastructure.security.query.AuthUserClientScopesQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScopePolicyService {

    private final AuthUserClientScopesQueryService scopesQueryService;

    public Set<String> resolveScopes(AuthUserType role, String clientId, Set<String> authorizedScopes) {
        Set<String> allowedScopes = scopesQueryService.findAllowedScopesByRoleAndClientId(role, clientId);

        if (authorizedScopes.isEmpty()) {
            return authorizedScopes;
        }

        return authorizedScopes.stream()
                .filter(allowedScopes::contains)
                .collect(Collectors.toSet());
    }
}
