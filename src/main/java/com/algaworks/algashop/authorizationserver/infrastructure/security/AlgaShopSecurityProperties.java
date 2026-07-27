package com.algaworks.algashop.authorizationserver.infrastructure.security;

import jakarta.validation.Valid;
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

    @NotNull
    @Valid
    private CorsProperties cors;

    @Data
    @NoArgsConstructor
    public static class CorsProperties {
        @NotEmpty
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
