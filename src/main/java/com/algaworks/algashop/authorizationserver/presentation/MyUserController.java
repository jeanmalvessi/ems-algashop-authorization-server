package com.algaworks.algashop.authorizationserver.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class MyUserController {

    @GetMapping
    public void getMe() {
    }
}
