package com.wrox.site.services;

import com.wrox.site.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProfileService {
    public Page<UserProfile> fetchOrganizers(Pageable p);
    public UserProfile fetchProfile(long profileId);
    public UserProfile save(UserProfile profile);
}
