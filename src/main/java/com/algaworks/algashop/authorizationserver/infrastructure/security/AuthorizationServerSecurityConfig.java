package com.algaworks.algashop.authorizationserver.infrastructure.security;

import com.algaworks.algashop.authorizationserver.infrastructure.security.code.DelegatingAuthorizationCodeRequestValidator;
import com.algaworks.algashop.authorizationserver.infrastructure.security.oidc.OidcUserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AuthorizationServerSecurityConfig {

    private final OidcUserInfoMapper oidcUserInfoMapper;
    private final OidcLogoutAuthenticationSuccessHandler oidcLogoutAuthenticationSuccessHandler;
    private final AlgaShopSecurityProperties algaShopSecurityProperties;
    private final DelegatingAuthorizationCodeRequestValidator delegatingAuthorizationCodeRequestValidator;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerFilterChain(HttpSecurity http) {
        var authorizationServer = new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServer.getEndpointsMatcher())
                .cors(Customizer.withDefaults())
                .headers(headers -> {
                    var csp = algaShopSecurityProperties.getCsp();
                    headers.contentSecurityPolicy(c -> c.policyDirectives(csp.getPolicyDirectives()));
                })
                .with(authorizationServer, configurer -> configurer
                        .oidc(oidc -> oidc
                            .logoutEndpoint(logout -> logout.logoutResponseHandler(oidcLogoutAuthenticationSuccessHandler))
                            .userInfoEndpoint(userInfo -> userInfo.userInfoMapper(oidcUserInfoMapper)))
                        .authorizationEndpoint(endpoint -> endpoint
                                .authenticationProviders(this::customizeAuthenticationProviders)
                                .consentPage("/oauth2/consent")))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .exceptionHandling(
                        exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) {
        http.securityMatcher("/change-password", "/forgot-password")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(RequestCacheConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
        http.securityMatcher("/api/**", "/actuator/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(c -> c
                        .loginPage("/login")
                        .defaultSuccessUrl(algaShopSecurityProperties.getDefaultRedirectUri())
                        .permitAll());

        return http.build();
    }

    private void customizeAuthenticationProviders(List<AuthenticationProvider> authenticationProviders) {
        authenticationProviders.stream()
                .filter(OAuth2AuthorizationCodeRequestAuthenticationProvider.class::isInstance)
                .map(OAuth2AuthorizationCodeRequestAuthenticationProvider.class::cast)
                .forEach(provider -> provider.setAuthenticationValidator(delegatingAuthorizationCodeRequestValidator));
    }
}
