package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@RestEndpoint
public class EventController {

    @Inject
    EventService eventService;

    @RequestMapping(value = "events", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> fetchAll(@PageableDefault(page = 0, size = 5) Pageable page){
        Page<Event> eventPage = eventService.getEvents(page);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.GET)
    public ResponseEntity<Event> fetchById(@PathVariable long eventId){
        return new ResponseEntity<>(eventService.getEventDetail(eventId), HttpStatus.OK);
    }

    @RequestMapping(value = "events/byOrganizer/{organizerId}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> fetchByOrganizer(@PathVariable long organizerId,
                                                              @PageableDefault(page = 0, size = 5) Pageable page){
        Page<Event> eventPage = eventService.getEvents(organizerId, page);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    @RequestMapping(value = "events", method = RequestMethod.POST)
    public ResponseEntity<Event> create(@RequestBody EventForm eventForm,
                                        @AuthenticationPrincipal UserPrincipal principal){
        if(principal == null ||
                principal.getAuthorities().stream().anyMatch(r -> "Event Organizer".equals(r.getAuthority()))){
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        Event newEvent = new Event();
        newEvent.setOnline(eventForm.online);
        newEvent.setContent(eventForm.content);
        newEvent.setSummary(eventForm.summary);
        newEvent.setStartDate(eventForm.startDate);
        newEvent.setEndDate(eventForm.endDate);
        newEvent.setTitle(eventForm.title);
        newEvent.setTags(eventForm.tags);
        newEvent.setOrganizerNames(eventForm.organizerNames);
        newEvent = eventService.saveEvent(newEvent,principal.getId(), eventForm.categoryIds, eventForm.statusId);
        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('Event Organizer')")
    public ResponseEntity<Event> edit(@RequestBody EventForm eventForm, @PathVariable long eventId,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal){
        Event editedEvent = eventService.getEventDetail(eventId);
        if(userPrincipal == null ||
                editedEvent.getUserProfile().getId() != userPrincipal.getId()){
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        editedEvent.setOnline(eventForm.online);
        editedEvent.setContent(eventForm.content);
        editedEvent.setSummary(eventForm.summary);
        editedEvent.setStartDate(eventForm.startDate);
        editedEvent.setEndDate(eventForm.endDate);
        editedEvent.setTitle(eventForm.title);
        editedEvent.setTags(eventForm.tags);
        editedEvent.setOrganizerNames(eventForm.organizerNames);
        editedEvent = eventService.saveEvent(editedEvent,0, eventForm.categoryIds, eventForm.statusId);
        return new ResponseEntity<>(editedEvent, HttpStatus.ACCEPTED);
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

        public EventForm() {
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