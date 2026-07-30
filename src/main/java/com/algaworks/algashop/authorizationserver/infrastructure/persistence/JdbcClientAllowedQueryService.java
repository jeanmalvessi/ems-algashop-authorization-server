package com.algaworks.algashop.authorizationserver.infrastructure.persistence;

import com.algaworks.algashop.authorizationserver.domain.model.user.AuthUserType;
import com.algaworks.algashop.authorizationserver.infrastructure.security.query.ClientAllowedQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JdbcClientAllowedQueryService implements ClientAllowedQueryService {

    private static final String SQL = """
			SELECT client_id
			FROM auth_user_type_client_allowed
			WHERE auth_user_type = ?
			""";

    private final JdbcOperations jdbcOperations;

    @Override
    public Set<String> findByRole(AuthUserType role) {
        return new HashSet<>(jdbcOperations.queryForList(SQL, String.class, role.name()));
    }
}
