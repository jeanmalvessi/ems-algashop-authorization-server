package com.algaworks.algashop.authorizationserver.application.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Component
@Data
@Validated
@ConfigurationProperties("algashop.user-account")
@NoArgsConstructor
public class UserAccountProperties {

    @NotNull
    @Valid
    private PasswordToken token;

    @NotNull
    @Valid
    private Mail mail;

    @Data
    @NoArgsConstructor
    public static class PasswordToken {
        @NotNull
        private Duration activationTtl;
        @NotNull
        private Duration passwordResetTtl;
    }

    @Data
    @NoArgsConstructor
    public static class Mail {
        @NotBlank
        private String passwordChangeUrl;
        @NotBlank
        private String from;
    }
}
