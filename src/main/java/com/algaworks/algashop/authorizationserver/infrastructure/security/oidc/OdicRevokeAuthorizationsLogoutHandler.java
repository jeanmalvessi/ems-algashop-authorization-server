package com.algaworks.algashop.authorizationserver.infrastructure.security.oidc;

import com.algaworks.algashop.authorizationserver.infrastructure.security.query.OAuth2AuthorizationQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OdicRevokeAuthorizationsLogoutHandler implements LogoutHandler {

    private final OAuth2AuthorizationQueryService auth2AuthorizationQueryService;
    private final OAuth2AuthorizationService authorizationService;
    private final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return;
        }
        performLogout(request, response, authentication);
        revokeAuthorizations(authentication);
    }

    private void performLogout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OidcLogoutAuthenticationToken oidcLogoutAuthentication = (OidcLogoutAuthenticationToken) authentication;
        if (oidcLogoutAuthentication.isPrincipalAuthenticated()) {
            this.securityContextLogoutHandler.logout(request, response, (Authentication) oidcLogoutAuthentication.getPrincipal());
        }
    }

    private void revokeAuthorizations(Authentication authentication) {
        String email = authentication.getName();
        List<String> authorizationIds = auth2AuthorizationQueryService.findAuthorizationIds(email);
        for (String authorizationId : authorizationIds) {
            OAuth2Authorization authorization = authorizationService.findById(authorizationId);
            if (authorization != null) {
                authorizationService.remove(authorization);
            }
        }
    }
}
