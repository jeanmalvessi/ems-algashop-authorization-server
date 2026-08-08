package com.algaworks.algashop.authorizationserver.infrastructure.security.password;

import com.algaworks.algashop.authorizationserver.domain.model.user.VerificationTokenHasher;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class VerificationTokenHasherImpl implements VerificationTokenHasher {

    @Override
    public String generate() {
        return RandomStringUtils.secure().nextAlphabetic(24);
    }

    @Override
    @SneakyThrows
    public String hash(String plainToken) {
        MessageDigest messageHasher = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageHasher.digest(plainToken.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    @Override
    public boolean isEqual(String hashed, String plainToken) {
        return MessageDigest.isEqual(
            hashed.getBytes(StandardCharsets.UTF_8),
            hash(plainToken).getBytes(StandardCharsets.UTF_8)
        );
    }
}
