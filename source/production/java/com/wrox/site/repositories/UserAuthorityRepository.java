package com.wrox.site.repositories;

import com.wrox.site.entities.UserAuthority;
import org.springframework.data.repository.CrudRepository;

public interface UserAuthorityRepository extends CrudRepository<UserAuthority, Integer> {
    UserAuthority getByAuthority(String role);
}
