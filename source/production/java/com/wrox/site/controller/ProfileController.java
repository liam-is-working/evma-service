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
    public ModelAndView getCurrentUser(@AuthenticationPrincipal UserPrincipal principal){
        if(principal == null)
            return new ModelAndView(new RedirectView("/api/login?error", true, false));
        long userId = principal.getId();
        return new ModelAndView(new RedirectView("/api/profiles/"+userId, true, false));
    }

    @RequestMapping(value = "profiles/{profileId}", method = RequestMethod.GET)
    public ResponseEntity<UserProfile> fetchById(@PathVariable long profileId){
        UserProfile profile = profileService.fetchProfile(profileId);
        if(profile == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(profile, HttpStatus.OK);
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
        if(profile == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        profile.setAddress(form.address);
        profile.setCity(form.city);
        profile.setDOB(form.DOB);
        profile.setEmail(form.email);
        profile.setJobTitle(form.jobTitle);
        profile.setPhoneNumber(form.phoneNumber);
        profile.setName(form.name);
        profile.setSummary(form.summary);
        return new ResponseEntity<>(profileService.save(profile), HttpStatus.ACCEPTED);
    }

    public static class ProfileForm implements Serializable {
        @NotBlank(message = "Name must not blank")
        @Size(min = 1, max = 50, message = "Name < 50")
        private String name;

        @Email(message = "Not a valid email")
        @NotBlank(message = "Email must not blank")
        private String email;

        private Instant DOB;

        @Size(min =1, max = 50, message = "City < 50")
        @Name(message = "Not a valid name")
        private String city;

        @Size(min = 1, max = 25, message = "Title < 25")
        @Name(message = "Not a valid job title")
        private String jobTitle;

        @Size(min = 1, max = 50, message = "Address < 50")
        @Address(message = "Not a valid address")
        private String address;

        @PhoneNumber(message = "Not a valid phone number")
        private String phoneNumber;

        @Size(max = 255, message = "Summary < 255")
        private String summary;

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

    }
}
