package com.wrox.site.repositories;

import com.wrox.site.entities.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrincipalRepository extends PagingAndSortingRepository<UserPrincipal, Long> {
    public UserPrincipal getUserPrincipalByUsername(String username);
    public Page<UserPrincipal> getUserPrincipalByUsernameLike(String username, Pageable p);
    public Page<UserPrincipal> getUserPrincipalByEnabled(boolean enable, Pageable p);
}
