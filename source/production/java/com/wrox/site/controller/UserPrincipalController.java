package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.config.annotation.WebController;
import com.wrox.site.entities.*;
import com.wrox.site.services.JwtTokenProvider;
import com.wrox.site.services.ProfileService;
import com.wrox.site.services.RoleService;
import com.wrox.site.services.UserPrincipalService;
import com.wrox.site.validation.Email;
import com.wrox.site.validation.NotBlank;
import javafx.geometry.Pos;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@RestEndpoint
public class UserPrincipalController
{
    @Inject
    UserPrincipalService userPrincipalService;
    @Inject
    ProfileService profileService;
    @Inject
    AuthenticationManager authenticationManager;
    @Inject
    JwtTokenProvider tokenProvider;
    @Inject
    RoleService roleService;

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
        newProfile.setRole(roleService.getRole(signupForm.role));
        newProfile.setEmail(signupForm.email);
        newProfile.setName(signupForm.name);
        newProfile.setDOB(signupForm.DOB);
        newProfile.setAvatarURL("ava_" + newProfile.getId());
        newProfile.setBackgroundURL("background_" + newProfile.getId());
        profileService.save(newProfile);
        return new ResponseEntity<>(newUser.getId(),HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<LoginResponse> login(@RequestParam String username, @RequestParam String password)
    {
        Authentication authentication = null;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password
                    )
            );
        }catch (AuthenticationException authenticationException){
            LoginResponse response = new LoginResponse();
            response.setStatus("Login fail");
            return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
        }

        // Nếu không xảy ra exception tức là thông tin hợp lệ
        // Set thông tin authentication vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Trả về jwt cho người dùng.
        String jwt = tokenProvider.generateToken((UserPrincipal) authentication.getPrincipal());
        LoginResponse response = new LoginResponse();
        response.setStatus("Login success");
        response.setToken(jwt);
        response.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    public static class LoginResponse{
        String status;
        String token;
        Authentication userPrincipal;

        public Authentication getUserPrincipal() {
            return userPrincipal;
        }

        public void setUserPrincipal(Authentication userPrincipal) {
            this.userPrincipal = userPrincipal;
        }

        public LoginResponse() {
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
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
        @NotBlank
        @NotNull
        public String signUsername;
        @NotBlank
        @NotNull
        public String signPassword;
        @NotBlank
        @NotNull
        public String role;
        @NotBlank
        @NotNull
        public String name;
        @Email
        @NotNull
        public String email;
        @NotNull
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
