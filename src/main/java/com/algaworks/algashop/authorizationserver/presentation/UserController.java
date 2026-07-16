package com.algaworks.algashop.authorizationserver.presentation;

import com.algaworks.algashop.authorizationserver.application.user.management.AuthUserInput;
import com.algaworks.algashop.authorizationserver.application.user.management.AuthUserManagementApplicationService;
import com.algaworks.algashop.authorizationserver.application.user.query.AuthUserOutput;
import com.algaworks.algashop.authorizationserver.infrastructure.security.check.SecurityAnnotations.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthUserManagementApplicationService managementService;

    @PostMapping
    @CanWriteUsers
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserOutput create(@RequestBody @Valid AuthUserInput input) {
        return managementService.create(input);
    }
}
