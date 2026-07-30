package com.algaworks.algashop.authorizationserver.infrastructure.security.code;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUser;
import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserRepository;
import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;
import com.algaworks.algashop.authorizationserver.infrastructure.security.query.ClientAllowedQueryService;
import com.algaworks.algashop.authorizationserver.infrastructure.security.token.ScopePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class AuthUserClientAccessPolicyValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    private final AuthUserRepository authUserRepository;
    private final ClientAllowedQueryService clientAllowedQueryService;
    private final ScopePolicyService scopePolicyService;

    @Override
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        OAuth2AuthorizationCodeRequestAuthenticationToken authentication = context.getAuthentication();
        Authentication principal = (Authentication) authentication.getPrincipal();

        if (principal == null || !principal.isAuthenticated() || principal instanceof AnonymousAuthenticationToken) {
            return;
        }

        String email = principal.getName();
        String clientId = authentication.getClientId();

        AuthUser authUser = authUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

        if (!canUseClient(authUser.getType(), clientId)) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED, "The authenticated user type is not allowed to authorize this client.", null);
            var codeRequest = buildCodeRequest(authentication, principal);
            throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, codeRequest);
        }

        Set<String> unauthorizedScopes = getUnauthorizedScopes(authentication, authUser.getType());
        if (!unauthorizedScopes.isEmpty()) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE, "The authenticated user type is not allowed to use this scopes: " + unauthorizedScopes, null);
            var codeRequest = buildCodeRequest(authentication, principal);
            throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, codeRequest);
        }
    }

    private OAuth2AuthorizationCodeRequestAuthenticationToken buildCodeRequest(OAuth2AuthorizationCodeRequestAuthenticationToken authentication, Authentication principal) {
        var resultToken = new OAuth2AuthorizationCodeRequestAuthenticationToken(
            authentication.getAuthorizationUri(),
            authentication.getClientId(),
            principal,
            authentication.getRedirectUri(),
            authentication.getState(),
            authentication.getScopes(),
            authentication.getAdditionalParameters()
        );
        resultToken.setAuthenticated(true);
        return resultToken;
    }

    private boolean canUseClient(AuthUserType type, String clientId) {
        Set<String> allowedClients = clientAllowedQueryService.findByRole(type);
        return allowedClients.contains(clientId);
    }

    private Set<String> getUnauthorizedScopes(OAuth2AuthorizationCodeRequestAuthenticationToken authentication, AuthUserType type) {
        Set<String> originalScopes = authentication.getScopes();
        Set<String> filteredScopes = scopePolicyService.resolveScopes(type, authentication.getClientId(), originalScopes);
        HashSet<String> result = new HashSet<>(originalScopes);
        result.removeAll(filteredScopes);
        return result;
    }
}
