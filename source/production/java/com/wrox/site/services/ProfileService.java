package com.wrox.site.services;

import com.wrox.site.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

@Validated
public interface ProfileService {
    public Page<UserProfile> fetchOrganizers(Pageable p);
    public UserProfile fetchProfile(long profileId);
    public UserProfile save(@Valid UserProfile profile);
}
