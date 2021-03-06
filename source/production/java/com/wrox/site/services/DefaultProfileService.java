package com.wrox.site.services;

import com.wrox.site.entities.UserProfile;
import com.wrox.site.repositories.UserAuthorityRepository;
import com.wrox.site.repositories.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class DefaultProfileService implements ProfileService{
    @Inject
    UserProfileRepository userProfile;
    @Inject
    UserAuthorityRepository authority;

    @Override
    @Transactional
    public Page<UserProfile> fetchOrganizers(Pageable p) {
        return userProfile.getByRole(authority.getByAuthority("Event Organizer"), p);
    }

    @Override
    @Transactional
    public UserProfile fetchProfile(long profileId) {
        return userProfile.findOne(profileId);
    }

    @Override
    @Transactional
    public UserProfile save(UserProfile profile) {
        userProfile.save(profile);
        if(profile.getAvatarURL() ==null|| profile.getBackgroundURL()==null){
            profile.setBackgroundURL("userBackground_"+profile.getId());
            profile.setAvatarURL("userAvatar_"+profile.getId());
        }
        return profile;
    }

    @Override
    public Page<UserProfile> fetchOrganizers(Pageable p, List<Long> ids) {
        return userProfile.getByRoleAndIdIn(authority.getByAuthority("Event Organizer"), p, ids );
    }

    @Override
    public Page<UserProfile> searchOrgByName(String name, Pageable p) {
        return userProfile.getByRoleAndNameContaining(authority.getByAuthority("Event Organizer"),p, name );
    }
}
