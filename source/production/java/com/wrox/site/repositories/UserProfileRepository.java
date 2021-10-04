package com.wrox.site.repositories;

import com.wrox.site.entities.UserAuthority;
import com.wrox.site.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends PagingAndSortingRepository<UserProfile, Long> {
    public Page<UserProfile> getByRole(UserAuthority role, Pageable p);
}
