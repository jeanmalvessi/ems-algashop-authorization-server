package com.algaworks.algashop.authorizationserver.infrastructure.security.jwk;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "jwk")
public class JwkProperties {
    @NotBlank
    private String privateKeyId;
    @NotBlank
    private String privateKey;
}
