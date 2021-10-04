package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.entities.UserProfile;
import com.wrox.site.services.ProfileService;
import com.wrox.site.validation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.persistence.Convert;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

@RestEndpoint
public class ProfileController {
    @Inject
    ProfileService profileService;

    @RequestMapping(value = "profiles/currentUser", method = RequestMethod.GET)
    public ModelAndView getCurrentUser(@AuthenticationPrincipal UserPrincipal principal,
                                       HttpServletResponse response) throws IOException {
        if(principal == null)
            return new ModelAndView(new RedirectView("/api/login?error", true, false));
        long userId = principal.getId();
        return new ModelAndView(new RedirectView("/api/profiles/"+userId, true, false));
    }

    @RequestMapping(value = "profiles/{profileId}", method = RequestMethod.GET)
    public ResponseEntity<UserProfile> fetchById(@PathVariable long profileId){
        return new ResponseEntity<>(profileService.fetchProfile(profileId), HttpStatus.OK);
    }

    @RequestMapping(value = "profiles/organizers", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<UserProfile>> fetchAllOrganizer(@PageableDefault(size = 5, page = 0) Pageable p){
        Page<UserProfile> profilePage = profileService.fetchOrganizers(p);
        return new ResponseEntity<>(new PageEntity<>(profilePage), HttpStatus.OK);
    }

    @RequestMapping(value = "profiles/{profileId}", method = RequestMethod.PUT)
    public ResponseEntity<UserProfile> updateProfile(@PathVariable long profileId,
                                                     @RequestBody ProfileForm form,
                                                     @AuthenticationPrincipal UserPrincipal principal){
        if(principal==null || principal.getId() != profileId)
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        UserProfile profile = profileService.fetchProfile(profileId);
        profile.setAddress(form.address);
        profile.setAvatarURL(form.avatarURL);
        profile.setCity(form.city);
        profile.setDOB(form.DOB);
        profile.setEmail(form.email);
        profile.setBackgroundURL(form.backgroundURL);
        profile.setJobTitle(form.jobTitle);
        profile.setPhoneNumber(form.phoneNumber);
        profile.setName(form.name);
        profile.setSummary(form.summary);
        return new ResponseEntity<>(profileService.save(profile), HttpStatus.ACCEPTED);
    }

    public static class ProfileForm implements Serializable {
        @NotBlank
        @Size(min = 1, max = 50)
        private String name;

        @Email
        @NotBlank
        private String email;

        private Instant DOB;

        @Size(min =1, max = 50)
        @Name
        private String city;

        @Size(min = 1, max = 25)
        @Name
        private String jobTitle;

        @Size(min = 1, max = 50)
        @Address
        private String address;

        @PhoneNumber
        private String phoneNumber;

        @Size(max = 250)
        private String summary;

        private String avatarURL;

        private String backgroundURL;

        public ProfileForm() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Instant getDOB() {
            return DOB;
        }

        public void setDOB(Instant DOB) {
            this.DOB = DOB;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getAvatarURL() {
            return avatarURL;
        }

        public void setAvatarURL(String avatarURL) {
            this.avatarURL = avatarURL;
        }

        public String getBackgroundURL() {
            return backgroundURL;
        }

        public void setBackgroundURL(String backgroundURL) {
            this.backgroundURL = backgroundURL;
        }
    }
}
