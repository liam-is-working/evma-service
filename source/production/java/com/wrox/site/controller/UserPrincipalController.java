package com.wrox.site.controller;

import com.google.firebase.auth.*;
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
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
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

//    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
//    public ResponseEntity<Void> changePassword(@RequestParam String newPassword,
//                                               @AuthenticationPrincipal UserPrincipal userPrincipal){
//        if(userPrincipal==null)
//            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//
//        userPrincipalService.saveUser(userPrincipal, newPassword, null);
//        return new ResponseEntity<>(HttpStatus.ACCEPTED);
//    }

   // @RequestMapping(value = "/signup", method = RequestMethod.POST)
    private void signup( SignupForm signupForm, boolean enable)
    {
        UserPrincipal newUser = new UserPrincipal();
        newUser.setUsername(signupForm.signUsername);
        newUser.setEnabled(enable);
        newUser.setCredentialsNonExpired(true);
        newUser.setAccountNonExpired(true);
        newUser.setAccountNonLocked(true);
        userPrincipalService.saveUser(newUser, signupForm.signPassword, signupForm.role);

        UserProfile newProfile = new UserProfile();
        newProfile.setId(newUser.getId());
        if(signupForm.role != null){
            newProfile.setRole(roleService.getRole(signupForm.role));
        }
        newProfile.setEmail(signupForm.email);
        newProfile.setName(signupForm.name);
        newProfile.setDOB(Instant.now());
        newProfile.setAvatarURL("ava_" + newProfile.getId());
        newProfile.setBackgroundURL("background_" + newProfile.getId());
        profileService.save(newProfile);
    }

//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public ResponseEntity<LoginResponse> login(@RequestParam String username, @RequestParam String password)
//    {
//        Authentication authentication = null;
//        try{
//            authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            username,
//                            password
//                    )
//            );
//        }catch (AuthenticationException authenticationException){
//            LoginResponse response = new LoginResponse();
//            response.setStatus("Login fail");
//            return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
//        }
//
//        // Nếu không xảy ra exception tức là thông tin hợp lệ
//        // Set thông tin authentication vào Security Context
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // Trả về jwt cho người dùng.
//        String jwt = tokenProvider.generateToken((UserPrincipal) authentication.getPrincipal());
//        LoginResponse response = new LoginResponse();
//        response.setStatus("Login success");
//        response.setToken(jwt);
//        long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
//        response.setProfile(profileService.fetchProfile(userId));
//        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
//    }

    @RequestMapping(value = "firebaseToken", method = RequestMethod.GET)
    public ResponseEntity firebaseToken(@RequestParam String token) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        String uid = decodedToken.getUid();
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);

        //authen by GG account only
        if(userRecord.getProviderData().length==0)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        UserInfo info = userRecord.getProviderData()[0];
        if(!"google.com".equals(info.getProviderId()))
            return new ResponseEntity(HttpStatus.NOT_FOUND);


        UserPrincipal user;
        user = userPrincipalService.loadUserByUsername(info.getEmail());

        //check if email has been already created
        if(user==null){
            //create new account
            SignupForm signupForm = new SignupForm();
            signupForm.setName(info.getDisplayName());
            signupForm.setEmail(info.getEmail());
            signupForm.setSignUsername(info.getEmail());
            //create new account, set 'enable' to handle different scenarios
            signup(signupForm,true);
            user = userPrincipalService.loadUserByUsername(info.getEmail());
        }

        //failed to create new account
        if(user==null)
            return new ResponseEntity(null, HttpStatus.EXPECTATION_FAILED);
        if(!user.isEnabled()){
            LoginResponse response = new LoginResponse();
            response.setStatus("Unable");
            return new ResponseEntity(response, HttpStatus.OK);
        }


        Authentication authentication = new PreAuthenticatedAuthenticationToken(user,
                null, user.getAuthorities());
        authentication.setAuthenticated(true);

        // Set thông tin authentication vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Trả về jwt cho người dùng.
        String jwt = tokenProvider.generateToken((UserPrincipal) authentication.getPrincipal());
        LoginResponse response = new LoginResponse();
        response.setStatus("Login success");
        response.setToken(jwt);
        long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        response.setProfile(profileService.fetchProfile(userId));
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

//    @RequestMapping(value = "loginWithGoogleToken", method = RequestMethod.POST)
//    public ResponseEntity<LoginResponse> loginWithGG(@RequestParam String accessToken){
//        String url = "https://www.googleapis.com/oauth2/v1/userinfo?" + "access_token=" + accessToken;
//        RestTemplate template = new RestTemplate();
//        GGProfile profile = template.getForObject(url, GGProfile.class);
//
//        //check if email has been already created
//        UserPrincipal user;
//        user = userPrincipalService.loadUserByUsername(profile.email);
//
//        if(user==null){
//            SignupForm signupForm = new SignupForm();
//            signupForm.setName(profile.name);
//            signupForm.setEmail(profile.email);
//            signupForm.setSignUsername(profile.email);
//            //create new account, set 'enable' to handle different scenarios
//            signup(signupForm,true);
//            user = userPrincipalService.loadUserByUsername(profile.email);
//        }
//
//        //failed to create new account
//        if(user==null)
//            return new ResponseEntity(null, HttpStatus.EXPECTATION_FAILED);
//        if(!user.isEnabled()){
//            LoginResponse response = new LoginResponse();
//            response.setStatus("Unable");
//            return new ResponseEntity(response, HttpStatus.OK);
//        }
//
//
//        Authentication authentication = new PreAuthenticatedAuthenticationToken(user,
//                null, user.getAuthorities());
//        authentication.setAuthenticated(true);
//
//        // Set thông tin authentication vào Security Context
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // Trả về jwt cho người dùng.
//        String jwt = tokenProvider.generateToken((UserPrincipal) authentication.getPrincipal());
//        LoginResponse response = new LoginResponse();
//        response.setStatus("Login success");
//        response.setToken(jwt);
//        long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
//        response.setProfile(profileService.fetchProfile(userId));
//        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
//    }
//
//    @RequestMapping(value = "GGOAuthEndpoint", method = RequestMethod.GET)
//    public ResponseEntity googleOAuthEndpoint(@RequestParam(required = false) String code,
//                                    @RequestBody(required = false) GGResponse GGResponse){
//        if(code!=null){
//            RestTemplate template = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//            map.add("code",code);
//            map.add("client_id","28993631450-sinuo1lupftse4tbt8rtsq54hgi0fj0q.apps.googleusercontent.com");
//            map.add("client_secret","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");
//            map.add("grant_type","authorization_code");
//            map.add("redirect_uri","http://localhost:8080/support/api/GGOAuthEndpoint");
//
//
//            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
//
//            String url
//                    = "https://oauth2.googleapis.com/token";
//            ResponseEntity<GGResponse> accessResponse
//                    = template.postForEntity(url,entity,GGResponse.class);
//            String accessToken = accessResponse.getBody().access_token;
//            //return new ResponseEntity(accessToken, HttpStatus.ACCEPTED);
//
//            map.clear();
//            map.add("access_token",accessToken);
//
//            url = "https://www.googleapis.com/oauth2/v1/userinfo?" + "access_token=" + accessToken;
//            GGProfile profile = template.getForObject(url, GGProfile.class);
//
//            //check if email has been already created
//            UserPrincipal user;
//            user = userPrincipalService.loadUserByUsername(profile.email);
//
//            if(user==null){
//                SignupForm signupForm = new SignupForm();
//                signupForm.setName(profile.name);
//                signupForm.setEmail(profile.email);
//                signupForm.setSignUsername(profile.email);
//                //create new account, set 'enable' to handle different scenarios
//                signup(signupForm,true);
//                user = userPrincipalService.loadUserByUsername(profile.email);
//            }
//
//            //failed to create new account
//            if(user==null)
//                return new ResponseEntity(null, HttpStatus.EXPECTATION_FAILED);
//            if(!user.isEnabled()){
//                LoginResponse response = new LoginResponse();
//                response.setStatus("Unable");
//                return new ResponseEntity(response, HttpStatus.OK);
//            }
//
//
//            Authentication authentication = new PreAuthenticatedAuthenticationToken(user,
//                    null, user.getAuthorities());
//            authentication.setAuthenticated(true);
//
//            // Set thông tin authentication vào Security Context
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            // Trả về jwt cho người dùng.
//            String jwt = tokenProvider.generateToken((UserPrincipal) authentication.getPrincipal());
//            LoginResponse response = new LoginResponse();
//            response.setStatus("Login success");
//            response.setToken(jwt);
//            long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
//            response.setProfile(profileService.fetchProfile(userId));
//            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
//        }else
//            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//    }
//
//    @RequestMapping(value = "GGOAuthEndpoint", method = RequestMethod.POST)
//    public ResponseEntity<GGResponse> getToken(@RequestBody(required = false) GGResponse response){
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "loginWithGG", method = RequestMethod.GET)
//    public ResponseEntity<String> generateLoginLink() {
//        String url = ("https://accounts.google.com/o/oauth2/v2/auth?"
//                + "scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email" +
//                "%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile"
//                + "&access_type=offline"
//                + "&include_granted_scopes=true"
//                + "&response_type=code"
//                + "&state=state_parameter_passthrough_value"
//                + "&redirect_uri=http%3A//localhost:8080/support/api/GGOAuthEndpoint"
//                + "&client_id=28993631450-sinuo1lupftse4tbt8rtsq54hgi0fj0q.apps.googleusercontent.com");
//        return new ResponseEntity<>(url, HttpStatus.OK);
//    }

    private static class GGProfile{
        public String id;
        public String email;
        public String name;
        public boolean verified_email;
        public String given_name;
        public String family_name;
        public String picture;
        public String locale;
        public String hd;

        public GGProfile() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isVerified_email() {
            return verified_email;
        }

        public void setVerified_email(boolean verified_email) {
            this.verified_email = verified_email;
        }

        public String getGiven_name() {
            return given_name;
        }

        public void setGiven_name(String given_name) {
            this.given_name = given_name;
        }

        public String getFamily_name() {
            return family_name;
        }

        public void setFamily_name(String family_name) {
            this.family_name = family_name;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getHd() {
            return hd;
        }

        public void setHd(String hd) {
            this.hd = hd;
        }
    }

    private static class GGResponse{
        public String access_token;
        public String expires_in;
        public String token_type;
        public String scope;
        public String id_token ;

        public GGResponse() {
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(String expires_in) {
            this.expires_in = expires_in;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getId_token() {
            return id_token;
        }

        public void setId_token(String id_token) {
            this.id_token = id_token;
        }
    }

    public static class LoginResponse{
        String status;
        String token;
        UserProfile profile;

        public UserProfile getProfile() {
            return profile;
        }

        public void setProfile(UserProfile profile) {
            this.profile = profile;
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

    @RequestMapping(value = "/login?error", method = RequestMethod.GET)
    public ResponseEntity<Void> loginError()
    {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public ResponseEntity test(@RequestParam String username){
        return new ResponseEntity(userPrincipalService.loadUserByUsername(username), HttpStatus.ACCEPTED);
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
        @NotBlank
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
