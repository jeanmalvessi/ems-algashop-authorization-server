package com.algaworks.algashop.authorizationserver.infrastructure.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@Validated
@ConfigurationProperties("algashop.security")
@NoArgsConstructor
public class AlgaShopSecurityProperties {

    @NotBlank
    private String defaultRedirectUri;

    @NotNull
    @Valid
    private CorsProperties cors;

    @NotNull
    @Valid
    private CookieProperties cookie;

    @NotNull
    @Valid
    private CspProperties csp;

    @Data
    @NoArgsConstructor
    public static class CorsProperties {
        @NotEmpty
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class CookieProperties {
        @NotBlank
        private String sameSite;
        @NotBlank
        private String domainName;
        @NotNull
        private Boolean secure;
    }

    @Data
    @NoArgsConstructor
    public static class CspProperties {
        private String policyDirectives;
    }
}
