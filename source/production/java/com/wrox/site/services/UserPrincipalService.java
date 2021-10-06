package com.wrox.site.services;

import com.wrox.site.entities.UserPrincipal;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
public interface UserPrincipalService extends UserDetailsService {
    @Override
    UserPrincipal loadUserByUsername(String username);

    void saveUser(
                   @Valid UserPrincipal principal,
            String newPassword, String role
    );

    UserDetails loadUserById(Long userId);
}
