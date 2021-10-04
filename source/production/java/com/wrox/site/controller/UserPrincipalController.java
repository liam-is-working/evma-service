package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.config.annotation.WebController;
import com.wrox.site.entities.EventStatus;
import com.wrox.site.entities.Post;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.entities.UserProfile;
import com.wrox.site.services.ProfileService;
import com.wrox.site.services.UserPrincipalService;
import javafx.geometry.Pos;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@RestEndpoint
public class UserPrincipalController
{
    @Inject
    UserPrincipalService userPrincipalService;
    @Inject
    ProfileService profileService;

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ResponseEntity<Void> changePassword(@RequestParam String newPassword,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal){
        if(userPrincipal==null)
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        userPrincipalService.saveUser(userPrincipal, newPassword, null);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<Long> signup(@RequestBody SignupForm signupForm)
    {
        UserPrincipal newUser = new UserPrincipal();
        newUser.setUsername(signupForm.signUsername);
        newUser.setEnabled(true);
        newUser.setCredentialsNonExpired(true);
        newUser.setAccountNonExpired(true);
        newUser.setAccountNonLocked(true);
        userPrincipalService.saveUser(newUser, signupForm.signPassword, signupForm.role);

        UserProfile newProfile = new UserProfile();
        newProfile.setId(newUser.getId());
        newProfile.setEmail(signupForm.email);
        newProfile.setName(signupForm.name);
        newProfile.setDOB(signupForm.DOB);
        newProfile.setAvatarURL("ava_" + newProfile.getId());
        newProfile.setBackgroundURL("background_" + newProfile.getId());
        profileService.save(newProfile);
        return new ResponseEntity<>(newUser.getId(),HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<Void> login()
    {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/login?loggedOut", method = RequestMethod.GET)
    public ResponseEntity<Void> loggedOut()
    {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/login?error", method = RequestMethod.GET)
    public ResponseEntity<Void> loginError()
    {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public static class SignupForm{
        public String signUsername;
        public String signPassword;
        public String role;
        public String name;
        public String email;
        public Instant DOB;

        public SignupForm() {
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

        public String getSignUsername() {
            return signUsername;
        }

        public void setSignUsername(String signUsername) {
            this.signUsername = signUsername;
        }

        public String getSignPassword() {
            return signPassword;
        }

        public void setSignPassword(String signPassword) {
            this.signPassword = signPassword;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

}
