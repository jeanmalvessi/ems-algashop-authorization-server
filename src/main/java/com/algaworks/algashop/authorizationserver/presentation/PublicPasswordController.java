package com.algaworks.algashop.authorizationserver.presentation;

import com.algaworks.algashop.authorizationserver.application.user.management.PasswordManagementApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PublicPasswordController {

    private final PasswordManagementApplicationService passwordManagementService;

    @GetMapping("/change-password")
    public String passwordForm(
            @RequestParam(name = "token", required = false)
            String token,
            Model model) {
        
        if (token == null || token.isBlank()) {
            model.addAttribute("message", "Invalid token.");
            model.addAttribute("success", false);
            return "password-message";
        }

        model.addAttribute("token", token);
        
        return "password-form";
    }

    @PostMapping(path = "/change-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String changePassword(@RequestParam("token") String token,
                                 @RequestParam("newPassword") String newPassword,
                                 Model model) {
        
        try {
            passwordManagementService.changePasswordWithToken(token, newPassword);
            model.addAttribute("message", "Password changed successfully.");
            model.addAttribute("success", true);
        } catch (AccessDeniedException e) {
            model.addAttribute("message", "Invalid token.");
            model.addAttribute("success", false);
        }

        return "password-message";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordProcessing(@RequestParam("email") String email) {
        passwordManagementService.requestPasswordChange(email);
        return "forgot-password-message";
    }
}
