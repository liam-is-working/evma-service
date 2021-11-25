package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.UserAuthority;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.entities.UserProfile;
import com.wrox.site.services.*;
import com.wrox.site.validation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestEndpoint
public class ProfileController {
    @Inject
    ProfileService profileService;
    @Inject
    FirebaseService firebaseService;
    @Inject
    EventService eventService;
    @Inject
    RoleService roleService;
    @Inject
    UserPrincipalService userPrincipalService;

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
                                                     @AuthenticationPrincipal UserPrincipal principal) throws ExecutionException, InterruptedException {
        if(principal==null || principal.getId() != profileId || principal.isEnabled() == false)
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        UserProfile profile = profileService.fetchProfile(profileId);
        if(profile == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        //Notify flag
        boolean orgChangeName = false;
        String oldName = profile.getName();
        profile.setAddress(form.address);

        //see if org change name
        if(profile.getRole() != null && form.name !=null
                && "Event Organizer".equals(profile.getRole().getAuthority())
                && !profile.getName().equalsIgnoreCase(form.name))
            orgChangeName = true;
        if(form.role!=null){
            //hardcoded
            //If update to these roles, account will be unable till admin approves
            if("Event Organizer".equals(form.role) || "Admin".equals(form.role)){
                principal.setEnabled(false);
                userPrincipalService.saveUser(principal, null, form.role);
            }else if("Attendees".equals(form.role)){
                principal.setEnabled(true);
                userPrincipalService.saveUser(principal, null, form.role);
            }
            profile.setRole(roleService.getRole(form.role));
        }
        profile.setCity(form.city);
        profile.setDOB(form.DOB);
        profile.setEmail(form.email);
        profile.setJobTitle(form.jobTitle);
        profile.setPhoneNumber(form.phoneNumber);
        profile.setName(form.name);
        profile.setSummary(form.summary);
        profile = profileService.save(profile);
        if(orgChangeName)
            firebaseService.notify(profileId, FirebaseService.NotificationTrigger.CHANGE_ORGANIZER_NAME,
                    FirebaseService.Issuer.ORGANIZER, oldName);
        return new ResponseEntity<>(profile, HttpStatus.ACCEPTED);
    }
    @RequestMapping(value = "followEvent", method = RequestMethod.GET)
    public ResponseEntity followEvent(@AuthenticationPrincipal UserPrincipal principal,
                                      @RequestParam long eventId) throws ExecutionException, InterruptedException {
        if(principal == null || principal.isEnabled() == false ||  principal.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals("Attendees")))
            return new ResponseEntity(null, HttpStatus.FORBIDDEN);
        return new ResponseEntity(firebaseService.followEvent(principal.getId(), eventId, FirebaseService.FollowOperation.FOLLOW_EVENT), HttpStatus.ACCEPTED);
    }
    @RequestMapping(value = "unfollowEvent", method = RequestMethod.GET)
    public ResponseEntity unfollowEvent(@AuthenticationPrincipal UserPrincipal principal,
                                        @RequestParam long eventId) throws ExecutionException, InterruptedException {
        if(principal == null || principal.isEnabled() == false ||  principal.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals("Attendees")))
            return new ResponseEntity(null, HttpStatus.FORBIDDEN);
        return new ResponseEntity(firebaseService.followEvent(principal.getId(), eventId, FirebaseService.FollowOperation.UNFOLLOW_EVENT), HttpStatus.ACCEPTED);
    }
    @RequestMapping(value = "followOrganizer", method = RequestMethod.GET)
    public ResponseEntity followOrganizer(@AuthenticationPrincipal UserPrincipal principal,
                                          @RequestParam long organizerId) throws ExecutionException, InterruptedException {
        if(principal == null || principal.isEnabled() == false ||  principal.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals("Attendees")))
            return new ResponseEntity(null, HttpStatus.FORBIDDEN);
        return new ResponseEntity(firebaseService.followEvent(principal.getId(), organizerId, FirebaseService.FollowOperation.FOLLOW_ORGANIZER), HttpStatus.ACCEPTED);
    }
    @RequestMapping(value = "unfollowOrganizer", method = RequestMethod.GET)
    public ResponseEntity unfollowOrganizer(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestParam long organizerId) throws ExecutionException, InterruptedException {
        if(principal == null || principal.isEnabled() == false ||  principal.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals("Attendees")))
            return new ResponseEntity(null, HttpStatus.FORBIDDEN);
        return new ResponseEntity(firebaseService.followEvent(principal.getId(), organizerId, FirebaseService.FollowOperation.UNFOLLOW_ORGANIZER), HttpStatus.ACCEPTED);
    }
    @RequestMapping(value = "getFollowEvents",method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> getFollowedEvents(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PageableDefault Pageable p) throws ExecutionException, InterruptedException {
        if(principal==null || principal.isEnabled() == false ||  principal.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals("Attendees")))
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        List<Long> followIds = firebaseService.getFollow(principal.getId(), FirebaseService.Issuer.EVENT);
        if(followIds==null)
            return new ResponseEntity<>(null, HttpStatus.OK);
        Page<Event> resultPage = eventService.getFollowedEvent
                (followIds,p);
        return new ResponseEntity<>(new PageEntity<>(resultPage), HttpStatus.OK);
    }
    @RequestMapping(value = "getFollowOrganizers", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<UserProfile>> getFollowedOrganizers(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PageableDefault Pageable p) throws ExecutionException, InterruptedException {
        if(principal==null || principal.isEnabled() == false || principal.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals("Attendees")))
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        List<Long> followIds = firebaseService.getFollow(principal.getId(), FirebaseService.Issuer.ORGANIZER);
        if(followIds == null){
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
            Page<UserProfile> resultPage = profileService
                    .fetchOrganizers(p,followIds);
            return new ResponseEntity<>(new PageEntity<>(resultPage), HttpStatus.OK);

    }
    @RequestMapping(value = "profiles/searchOrganizers", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<UserProfile>> searchOrganizer(@RequestParam String orgName,
                                                                   @PageableDefault Pageable p){
        Page<UserProfile> profilePage = profileService.searchOrgByName(orgName, p);
        return new ResponseEntity<>(new PageEntity<>(profilePage), HttpStatus.OK);
    }

    public static class ProfileForm implements Serializable {
        @NotBlank(message = "Name must not blank")
        @Size(min = 1, max = 50, message = "Name < 50")
        private String name;

        public String role;

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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
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
