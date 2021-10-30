package com.wrox.site.services;

import com.wrox.site.entities.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Validated
public interface UserPrincipalService extends UserDetailsService {
    @Override
    UserPrincipal loadUserByUsername(String username);

    void saveUser(
                   @Valid UserPrincipal principal,
            String newPassword, String role
    );

    UserDetails loadUserById(Long userId);

    Page<UserPrincipal> loadAllUser(Pageable p);

    Page<UserPrincipal> loadUserByUsername(String name, Pageable p);

    Page<UserPrincipal> loadUsers(Pageable p, boolean enable);

    UserPrincipal switchState(long userId);

    UserPrincipal loadUser(Long userId);



}
