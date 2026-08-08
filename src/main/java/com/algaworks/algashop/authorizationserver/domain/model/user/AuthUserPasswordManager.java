package com.algaworks.algashop.authorizationserver.domain.model.user;

public interface AuthUserPasswordManager {
    String generate();
    String encrypt(String plainPassword);
    boolean matches(String encryptedPassword, String plainPassword);
}
