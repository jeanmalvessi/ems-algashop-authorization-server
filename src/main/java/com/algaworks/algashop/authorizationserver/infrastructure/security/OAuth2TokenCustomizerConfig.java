package com.algaworks.algashop.authorizationserver.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
@RequiredArgsConstructor
public class OAuth2TokenCustomizerConfig {

    private final OIDCUserInfoService oidcUserInfoService;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            String tokenType = context.getTokenType().getValue();
            AuthorizationGrantType authorizationGrantType = context.getAuthorizationGrantType();
            OidcUserInfo oidcUserInfo = loadUser(context);
            if (isIdToken(tokenType)) {
                context.getClaims().claims(claims -> claims.putAll(oidcUserInfo.getClaims()));
            } else if (isAccessToken(tokenType) && (isAuthCodeFlow(authorizationGrantType) || isRefreshTokenFlow(authorizationGrantType))) {
                context.getClaims().subject(oidcUserInfo.getSubject());
            }
        };
    }

    private boolean isRefreshTokenFlow(AuthorizationGrantType authorizationGrantType) {
        return AuthorizationGrantType.REFRESH_TOKEN.equals(authorizationGrantType);
    }

    private boolean isAuthCodeFlow(AuthorizationGrantType authorizationGrantType) {
        return AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType);
    }

    private OidcUserInfo loadUser(JwtEncodingContext context) {
        String email = context.getPrincipal().getName();
        return oidcUserInfoService.loadUser(email);
    }

    private boolean isAccessToken(String tokenType) {
        return OAuth2TokenType.ACCESS_TOKEN.getValue().equals(tokenType);
    }

    private boolean isIdToken(String tokenType) {
        return OidcParameterNames.ID_TOKEN.equals(tokenType);
    }
}
