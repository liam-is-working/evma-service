package com.wrox.site.repositories;

import com.wrox.site.entities.UserAuthority;
import com.wrox.site.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, Long> {
    public Page<UserProfile> getByRole(UserAuthority role, Pageable p);
    public Page<UserProfile> getByRoleAndIdIn(UserAuthority role, Pageable p, List<Long> ids);
}
