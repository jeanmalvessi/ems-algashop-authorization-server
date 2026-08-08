package com.algaworks.algashop.authorizationserver.application.user.mail;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUser;

public interface AuthUserMailSender {
    void sendActivationEmail(AuthUser user, String token);
    void sendPasswordChangeEmail(AuthUser user, String token);
}
