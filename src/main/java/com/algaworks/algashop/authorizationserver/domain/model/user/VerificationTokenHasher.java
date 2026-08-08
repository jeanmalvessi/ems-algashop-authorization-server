package com.algaworks.algashop.authorizationserver.domain.model.user;

public interface VerificationTokenHasher {
    String generate();
    String hash(String plainToken);
    boolean isEqual(String hashed, String plainToken);
}
