package com.algaworks.algashop.authorizationserver.infrastructure.security.code;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_REDIRECT_URI_VALIDATOR;
import static org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR;

@Component
@RequiredArgsConstructor
public class DelegatingAuthorizationCodeRequestValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    private final AuthUserClientAccessPolicyValidator clientAccessPolicyValidator;

    @Override
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext context) {
        DEFAULT_REDIRECT_URI_VALIDATOR
                .andThen(DEFAULT_SCOPE_VALIDATOR)
                .andThen(clientAccessPolicyValidator)
                .accept(context);
    }
}
