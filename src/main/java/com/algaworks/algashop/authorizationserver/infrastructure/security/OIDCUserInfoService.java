package com.algaworks.algashop.authorizationserver.infrastructure.security;

import com.algaworks.algashop.authorizationserver.domain.model.AuthUser;
import com.algaworks.algashop.authorizationserver.domain.model.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OIDCUserInfoService {

    private final AuthUserRepository authUserRepository;

    public OidcUserInfo loadUser(String email) {
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));

        return OidcUserInfo.builder()
                .subject(authUser.getId().toString())
                .name(authUser.getName())
                .email(authUser.getEmail())
                .claim("type", authUser.getType().name())
                .claim("created_at", authUser.getCreatedAt().toEpochSecond())
                .build();
    }
}
