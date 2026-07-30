package com.algaworks.algashop.authorizationserver.infrastructure.security.token;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;
import com.algaworks.algashop.authorizationserver.infrastructure.security.oidc.OidcUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final OidcUserInfoService oidcUserInfoService;
    private final ScopePolicyService scopePolicyService;

    @Override
    public void customize(JwtEncodingContext context) {
        String tokenType = context.getTokenType().getValue();
        AuthorizationGrantType authorizationGrantType = context.getAuthorizationGrantType();
        if (isIdToken(tokenType)) {
            customizeIdToken(context);
        } else if (isAccessToken(tokenType) && isUserDelegatedFlow(authorizationGrantType)) {
            customizeAccessToken(context);
        }
    }

    private void customizeIdToken(JwtEncodingContext context) {
        OidcUserInfo oidcUserInfo = loadUser(context);
        context.getClaims().claims(claims -> claims.putAll(oidcUserInfo.getClaims()));
    }

    private void customizeAccessToken(JwtEncodingContext context) {
        OidcUserInfo oidcUserInfo = loadUser(context);

        Set<String> filteredScopes = scopePolicyService.resolveScopes(
                AuthUserType.valueOf(oidcUserInfo.getClaimAsString("type")),
                context.getRegisteredClient().getClientId(),
                context.getAuthorizedScopes()
        );

        context.getClaims().subject(oidcUserInfo.getSubject());
        context.getClaims().claim("role", oidcUserInfo.getClaimAsString("type"));
        context.getClaims().claims(claim -> claim.put("scope", filteredScopes));
    }

    private OidcUserInfo loadUser(JwtEncodingContext context) {
        String email = context.getPrincipal().getName();
        return oidcUserInfoService.loadUser(email);
    }

    private boolean isUserDelegatedFlow(AuthorizationGrantType authorizationGrantType) {
        return isAuthCodeFlow(authorizationGrantType) || isRefreshTokenFlow(authorizationGrantType);
    }

    private boolean isRefreshTokenFlow(AuthorizationGrantType authorizationGrantType) {
        return AuthorizationGrantType.REFRESH_TOKEN.equals(authorizationGrantType);
    }

    private boolean isAuthCodeFlow(AuthorizationGrantType authorizationGrantType) {
        return AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType);
    }

    private boolean isAccessToken(String tokenType) {
        return OAuth2TokenType.ACCESS_TOKEN.getValue().equals(tokenType);
    }

    private boolean isIdToken(String tokenType) {
        return OidcParameterNames.ID_TOKEN.equals(tokenType);
    }
}
