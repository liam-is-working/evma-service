package com.wrox.site.services;

import com.wrox.site.entities.UserAuthority;
import com.wrox.site.repositories.UserAuthorityRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class DefaultRoleService implements RoleService{
    @Inject
    UserAuthorityRepository authorityRepository;

    @Override
    @Transactional
    public UserAuthority getRole(String roleName) {
        return authorityRepository.getByAuthority(roleName);
    }
}
