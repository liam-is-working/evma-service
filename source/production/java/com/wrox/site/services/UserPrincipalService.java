package com.wrox.site.services;

import com.wrox.site.entities.UserPrincipal;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface UserPrincipalService extends UserDetailsService {
    @Override
    UserPrincipal loadUserByUsername(String username);

    void saveUser(
                    UserPrincipal principal,
            String newPassword, String role
    );

    UserDetails loadUserById(Long userId);
}
