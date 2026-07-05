package com.algaworks.algashop.authorizationserver.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class OAuth2TokenCustomizerConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(OIDCUserInfoService oidcUserInfoService) {
        return context -> {
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                String email = context.getPrincipal().getName();
                OidcUserInfo oidcUserInfo = oidcUserInfoService.loadUser(email);
                context.getClaims().claims(claims -> claims.putAll(oidcUserInfo.getClaims()));
            }
        };
    }
}
