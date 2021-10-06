package com.wrox.site.services;

import com.wrox.site.entities.UserAuthority;

public interface RoleService {
    UserAuthority getRole(String roleName);
}
