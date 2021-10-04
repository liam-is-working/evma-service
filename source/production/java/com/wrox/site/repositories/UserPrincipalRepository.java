package com.wrox.site.repositories;

import com.wrox.site.entities.UserPrincipal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrincipalRepository extends CrudRepository<UserPrincipal, Long> {
    public UserPrincipal getUserPrincipalByUsername(String username);
}
