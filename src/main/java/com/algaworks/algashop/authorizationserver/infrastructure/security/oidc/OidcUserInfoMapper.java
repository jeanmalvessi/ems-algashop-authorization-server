package com.algaworks.algashop.authorizationserver.infrastructure.security.oidc;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OidcUserInfoMapper implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        var idTokenHolder = authorization.getToken(OidcIdToken.class);
        if (idTokenHolder == null) {
            Authentication authentication = context.getAuthentication();
            JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();
            if (principal == null || principal.getToken() == null) {
                throw new IllegalArgumentException();
            }
            return OidcUserInfo.builder()
                    .claim("sub", principal.getToken().getClaims().get("sub"))
                    .build();
        }
        return new OidcUserInfo(idTokenHolder.getClaims());
    }
}
