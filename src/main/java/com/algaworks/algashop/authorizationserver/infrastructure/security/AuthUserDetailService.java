package com.algaworks.algashop.authorizationserver.infrastructure.security;

import com.algaworks.algashop.authorizationserver.domain.model.AuthUser;
import com.algaworks.algashop.authorizationserver.domain.model.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetailService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));

        return User.withUsername(email)
                .password(authUser.getPassword())
                .disabled(!authUser.isEnabled())
                .build();
    }
}
