package com.algaworks.algashop.authorizationserver.infrastructure.security.jwk;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
public class JwkSourceConfig {

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwkProperties properties) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyId = properties.getPrivateKeyId();
        String privateKeyBase64 = properties.getPrivateKey();
        String privateKeyValue = new String(Base64.getDecoder().decode(privateKeyBase64), StandardCharsets.UTF_8);

        RSAPrivateKey rsaPrivateKey = RsaKeyConverters.pkcs8()
                .convert(new ByteArrayInputStream(privateKeyValue.getBytes(StandardCharsets.UTF_8)));

        RSAPrivateCrtKey privateCrtKey = (RSAPrivateCrtKey) rsaPrivateKey;

        RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(
                        privateCrtKey.getModulus(),
                        privateCrtKey.getPublicExponent()
                ));

        RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
                .privateKey(rsaPrivateKey)
                .keyID(privateKeyId)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }
}
