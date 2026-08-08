package com.algaworks.algashop.authorizationserver.application.user.management;

import com.algaworks.algashop.authorizationserver.application.user.UserAccountProperties;
import com.algaworks.algashop.authorizationserver.application.user.mail.AuthUserMailSender;
import com.algaworks.algashop.authorizationserver.application.user.query.AuthUserNotFoundException;
import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUser;
import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserPasswordManager;
import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserRepository;
import com.algaworks.algashop.authorizationserver.domain.model.user.VerificationTokenHasher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordManagementApplicationService {

    private final AuthUserRepository authUserRepository;
    private final UserAccountProperties userAccountProperties;
    private final AuthUserPasswordManager passwordManager;
    private final VerificationTokenHasher tokenHasher;
    private final AuthUserMailSender authUserMailSender;

    public void changePasswordWithToken(String plainToken, String newPlainPassword) {
        String hash = tokenHasher.hash(plainToken);
        AuthUser authUser = authUserRepository.findByVerificationToken(hash)
                .orElseThrow(() -> new AuthUserNotFoundException("User not found by verification token"));
        
        try {
            authUser.changePasswordWithToken(plainToken, newPlainPassword, passwordManager, tokenHasher);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new AccessDeniedException(e.getMessage());
        }
        
        authUserRepository.save(authUser);
    }

    public void requestPasswordChange(UUID userId) {
        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new AuthUserNotFoundException(userId));
        requestPasswordChange(authUser);
    }

    public void requestPasswordChange(String email) {
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new AuthUserNotFoundException(email));
        requestPasswordChange(authUser);
    }

    private void requestPasswordChange(AuthUser authUser) {
        String plainToken = authUser.generateVerificationToken(userAccountProperties.getToken().getPasswordResetTtl(), tokenHasher);

        authUserMailSender.sendPasswordChangeEmail(authUser, plainToken);

        authUserRepository.save(authUser);
    }
}
