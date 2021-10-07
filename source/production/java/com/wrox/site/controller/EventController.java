package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.exception.ResourceNotFoundException;
import com.wrox.site.entities.*;
import com.wrox.site.services.CategoryService;
import com.wrox.site.services.EventService;
import com.wrox.site.services.EventStatusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@RestEndpoint
public class EventController {

    @Inject
    EventService eventService;
    @Inject
    CategoryService categoryService;
    @Inject
    EventStatusService eventStatusService;

    @RequestMapping(value = "events", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> fetchPublishedEvent(@PageableDefault(page = 0, size = 5) Pageable page){
        Page<Event> eventPage = eventService.getPublishedEvent(page);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.GET)
    public ResponseEntity<Event> fetchById(@PathVariable long eventId,
                                           @AuthenticationPrincipal UserPrincipal principal){
        Event event = eventService.getEventDetail(eventId);
        if (event==null)
            throw new ResourceNotFoundException();
        if(!"Published".equals(event.getStatus().getName())){
            if(principal==null ||
                    (principal.getId()!=event.getUserProfileId()&&
                            principal.getAuthorities().stream().noneMatch(r -> "Admin".equals(r.getAuthority()))))
                return new ResponseEntity<>(null,HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(eventService.getEventDetail(eventId), HttpStatus.OK);
    }

    @RequestMapping(value = "events/byOrganizer/{organizerId}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> fetchByOrganizer(@PathVariable long organizerId,
                                                              @PageableDefault(page = 0, size = 5) Pageable page){
        Page<Event> eventPage = eventService.getPublishedEvent(organizerId, page);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    //test
    @RequestMapping(value = "events", method = RequestMethod.POST)
    public ResponseEntity<Event> create(@RequestBody @Valid EventForm eventForm,
                                        @AuthenticationPrincipal UserPrincipal principal
                                        ) {
        //Authorize
        if (principal == null ||
                principal.getAuthorities().stream().noneMatch(r -> "Event Organizer".equals(r.getAuthority()))) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        //Validation
//        if(errors.hasErrors()){
//            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//        }
            Event newEvent = new Event();
            newEvent.setOnline(eventForm.online);
            newEvent.setContent(eventForm.content);
            newEvent.setSummary(eventForm.summary);
            newEvent.setStartDate(eventForm.startDate);
            newEvent.setEndDate(eventForm.endDate);
            newEvent.setTitle(eventForm.title);
            newEvent.setTags(eventForm.tags);
            newEvent.setAddresses(eventForm.addresses);
            newEvent.setOrganizerNames(eventForm.organizerNames);
            newEvent.setUserProfileId(principal.getId());
            newEvent = eventService.saveEvent(newEvent, eventForm.categoryIds, eventForm.statusId);
            return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    @RequestMapping(value = "events/categories", method = RequestMethod.GET)
    public ResponseEntity<List<Category>> getCategories(){
        return new ResponseEntity<>(categoryService.getAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "events/status", method = RequestMethod.GET)
    public ResponseEntity<List<EventStatus>> getStatus(){
        return new ResponseEntity<>(eventStatusService.getStatus(), HttpStatus.OK);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.PUT)
    public ResponseEntity<Event> edit(@RequestBody @Valid EventForm eventForm,
                                      Errors errors,@PathVariable long eventId,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal){
        //authorize
        Event editedEvent = eventService.getEventDetail(eventId);
        if(userPrincipal == null || editedEvent == null ||
                editedEvent.getUserProfileId() != userPrincipal.getId()){
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        //validate
        if (errors.hasErrors()){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        editedEvent.setOnline(eventForm.online);
        editedEvent.setContent(eventForm.content);
        editedEvent.setSummary(eventForm.summary);
        editedEvent.setStartDate(eventForm.startDate);
        editedEvent.setEndDate(eventForm.endDate);
        editedEvent.setTitle(eventForm.title);
        editedEvent.setTags(eventForm.tags);
        editedEvent.setOrganizerNames(eventForm.organizerNames);
        editedEvent.setAddresses(eventForm.addresses);
        editedEvent = eventService.saveEvent(editedEvent, eventForm.categoryIds, eventForm.statusId);
        return new ResponseEntity<>(editedEvent, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.PATCH)
    public ResponseEntity changeEventStatus(@RequestBody eventStatusForm form,
                                            @PathVariable long eventId,
                                            @AuthenticationPrincipal UserPrincipal principal){
        //get event
        Event event = eventService.getEventDetail(eventId);
        if(event == null)
            throw new ResourceNotFoundException();
        //authorize
        if(principal==null ||
                (principal.getId()!=event.getUserProfileId()&&
                        principal.getAuthorities().stream().noneMatch(r -> "Admin".equals(r.getAuthority()))))
            return new ResponseEntity(HttpStatus.FORBIDDEN);

        eventService.saveEvent(event,null,form.getStatusId());
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "events/byOrganizer/{organizerId}/{statusName}")
    public ResponseEntity<PageEntity<Event>> getEventByStatus(@PathVariable(value = "organizerId") long organizerId,
                                                              @PathVariable(value = "statusName") String statusName,
                                                              @PageableDefault Pageable p,
                                                              @AuthenticationPrincipal UserPrincipal principal){
        if(principal==null || (principal.getId() != organizerId &&
        principal.getAuthorities().stream().anyMatch(r -> "Admin".equals(r.getAuthority()))))
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);


        Page<Event> eventPage = eventService.getEventByStatus(statusName,organizerId,p);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    public static class eventStatusForm{
        int statusId;

        public int getStatusId() {
            return statusId;
        }

        public void setStatusId(int statusId) {
            this.statusId = statusId;
        }
    }

    public static class EventForm{

        String title;

        String content;
        Set<String> tags;
        Set<String> organizerNames;

        boolean online;

        Instant startDate;
        Instant endDate;

        String summary;
        Set<Long> categoryIds;
        int statusId;
        Set<Address> addresses;

        public EventForm() {
        }

        public Set<Address> getAddresses() {
            return addresses;
        }

        public void setAddresses(Set<Address> addresses) {
            this.addresses = addresses;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public Set<String> getOrganizerNames() {
            return organizerNames;
        }

        public void setOrganizerNames(Set<String> organizerNames) {
            this.organizerNames = organizerNames;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public Instant getStartDate() {
            return startDate;
        }

        public void setStartDate(Instant startDate) {
            this.startDate = startDate;
        }

        public Instant getEndDate() {
            return endDate;
        }

        public void setEndDate(Instant endDate) {
            this.endDate = endDate;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Set<Long> getCategoryIds() {
            return categoryIds;
        }

        public void setCategoryIds(Set<Long> categoryIds) {
            this.categoryIds = categoryIds;
        }

        public int getStatusId() {
            return statusId;
        }

        public void setStatusId(int statusId) {
            this.statusId = statusId;
        }
    }
}
