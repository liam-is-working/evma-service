package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.site.entities.*;
import com.wrox.site.services.CategoryService;
import com.wrox.site.services.EventService;
import com.wrox.site.services.EventStatusService;
import com.wrox.site.services.FirebaseService;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestEndpoint
public class EventController {

    @Inject
    EventService eventService;
    @Inject
    CategoryService categoryService;
    @Inject
    EventStatusService eventStatusService;
    @Inject
    FirebaseService firebaseService;


    @RequestMapping(value = "events", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> fetchPublishedEvent(@PageableDefault(page = 0, size = 5) Pageable page){
        Page<Event> eventPage = eventService.getPublishedEvent(page);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    //Get all kinds of events with authorizing
    @RequestMapping(value = "events/{eventId}", method = RequestMethod.GET)
    public ResponseEntity<Event> fetchById(@PathVariable long eventId,
                                           @AuthenticationPrincipal UserPrincipal principal){
        Event event = eventService.getEventDetail(eventId);
        if (event==null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        if(!"Published".equals(event.getStatus().getName()) && !"Cancelled".equals(event.getStatus().getName())){
            if(principal==null || principal.isEnabled() == false ||
                    (principal.getId()!=event.getUserProfileId()&&
                            principal.getAuthorities().stream().noneMatch(r -> "Admin".equals(r.getAuthority()))))
                return new ResponseEntity<>(null,HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(eventService.getEventDetail(eventId), HttpStatus.OK);
    }

    //Get published events only
    @RequestMapping(value = "events/byOrganizer/{organizerId}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> fetchByOrganizer(@PathVariable long organizerId,
                                                              @PageableDefault(page = 0, size = 5) Pageable page){
        Page<Event> eventPage = eventService.getPublishedEvent(organizerId, page);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    @RequestMapping(value = "events", method = RequestMethod.POST)
    public ResponseEntity<Event> create(@RequestBody @Valid EventForm eventForm,
                                        @AuthenticationPrincipal UserPrincipal principal
                                        ) throws ExecutionException, InterruptedException {
        //Authorize
        if (principal == null || principal.isEnabled() == false||
                principal.getAuthorities().stream().noneMatch(r -> "Event Organizer".equals(r.getAuthority()))) {
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
            newEvent.setAddresses(eventForm.addresses);
            newEvent.setOrganizerNames(eventForm.organizerNames);
            newEvent.setUserProfileId(principal.getId());
            newEvent = eventService.saveEvent(newEvent, eventForm.categoryIds, eventForm.statusId);
            if("Published".equals(newEvent.getStatus().getName()))
                firebaseService.notify(principal.getId(), FirebaseService.NotificationTrigger.ADD_EVENT, FirebaseService.Issuer.ORGANIZER,null);
            return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    @RequestMapping(value = "events/categories", method = RequestMethod.GET)
    public ResponseEntity<Set<Category>> getCategories(){
        return new ResponseEntity<>(categoryService.getAvailable(), HttpStatus.OK);
    }

    @RequestMapping(value = "events/byCategory/{catId}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> getByCategory(@PathVariable long catId,
                                                       @PageableDefault Pageable p){
        Set<Category> categorySet = new HashSet<>();
        Category cat = categoryService.getById(catId);
        if(cat == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        categorySet.add(cat);
        Page<Event> eventPage = eventService.searchEvent(null,categorySet,null,null,null, null,p);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    @RequestMapping(value = "events/status", method = RequestMethod.GET)
    public ResponseEntity<List<EventStatus>> getStatus(){
        return new ResponseEntity<>(eventStatusService.getStatus(), HttpStatus.OK);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.PUT)
    public ResponseEntity<Event> edit(@RequestBody @Valid EventForm eventForm,
                                      Errors errors,@PathVariable long eventId,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal) throws ExecutionException, InterruptedException {

        //authorize
        Event editedEvent = eventService.getEventDetail(eventId);
        if(userPrincipal == null || userPrincipal.isEnabled() == false|| editedEvent == null ||
                editedEvent.getUserProfileId() != userPrincipal.getId()){
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        //validate event status
        if(editedEvent.getStatus().getName().equals("Cancelled") ||
                editedEvent.getStatus().getName().equals("Deleted"))
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        //Notification flag
        boolean isPublished = editedEvent.getStatus().getName().equals("Published");
        boolean updateTitle = !editedEvent.getTitle().equals(eventForm.title);
        String oldTitle = editedEvent.getTitle();

        if(editedEvent.getStatus().getId() != eventForm.getStatusId())
            updateEventStatus(editedEvent, eventForm.statusId);

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

        //Only noti if event is published
        if(isPublished){
            firebaseService.notify(eventId, FirebaseService.NotificationTrigger.UPDATE_EVENT_DETAILS,
                    FirebaseService.Issuer.EVENT, null);
            if(updateTitle)
                firebaseService.notify(eventId, FirebaseService.NotificationTrigger.CHANGE_EVENT_TITLE,
                        FirebaseService.Issuer.EVENT, oldTitle);
        }

        return new ResponseEntity<>(editedEvent, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "events/{eventId}", method = RequestMethod.PATCH)
    public ResponseEntity changeEventStatus(@RequestBody eventStatusForm form,
                                            @PathVariable long eventId,
                                            @AuthenticationPrincipal UserPrincipal principal) throws ExecutionException, InterruptedException {
        //get event
        Event event = eventService.getEventDetail(eventId);
        if(event == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        //authorize
        if(principal==null || principal.isEnabled() == false ||
                (principal.getId()!=event.getUserProfileId()&&
                        principal.getAuthorities().stream().noneMatch(r -> "Admin".equals(r.getAuthority()))))
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        updateEventStatus(event, form.statusId);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    private void updateEventStatus(Event event, int statusId) throws ExecutionException, InterruptedException {
        EventStatus changeStat = eventStatusService.getStatus(statusId);
        if(changeStat ==null)
            return;

        //Check if action is valid
        if("Deleted".equals(event.getStatus().getName()) || "Cancelled".equals(event.getStatus().getName())){
            return;
        }

        //Notification flag
        FirebaseService.NotificationTrigger trigger = null;
        boolean switchState = false;
        boolean addNew = false;
        //From published to other statuses
        if(event.getStatus().getName().equals("Published")){
            switchState = true;
            if(changeStat.getName().equals("Cancelled"))
                trigger = FirebaseService.NotificationTrigger.CANCEL_EVENT;
            if(changeStat.getName().equals("Draft"))
                trigger = FirebaseService.NotificationTrigger.POSTPONE_EVENT;
            if(changeStat.getName().equals("Deleted"))
                trigger = FirebaseService.NotificationTrigger.DELETE_EVENT;
            if(trigger == null)
                switchState = false;
        }
        //From Draft status to Published
        if(changeStat.getName().equals("Published") && event.getStatus().getName().equals("Draft")){
            addNew = true;
            trigger = FirebaseService.NotificationTrigger.ADD_EVENT;
        }

        event.setStatus(changeStat);
        eventService.saveEvent(event);
        if(switchState){
            firebaseService.notify(event.getId(),trigger, FirebaseService.Issuer.EVENT,null);
        }
        if(addNew){
            firebaseService.notify(event.getUserProfileId(),trigger, FirebaseService.Issuer.ORGANIZER, null);
        }
    }

    public static class ChangeEventStateMessage{
        public String currentStatus;
        public String targetStatus;
        public String message;

        public ChangeEventStateMessage(String currentStatus, String targetStatus, String message) {
            this.currentStatus = currentStatus;
            this.targetStatus = targetStatus;
            this.message = message;
        }
    }

    //url events/byOrganizer/a/published
    @RequestMapping(value = "events/byOrganizer/{organizerId}/{statusName}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Event>> getEventByStatus(@PathVariable(value = "organizerId") long organizerId,
                                                              @PathVariable(value = "statusName") String statusName,
                                                              @PageableDefault Pageable p,
                                                              @AuthenticationPrincipal UserPrincipal principal){
        if(principal==null || principal.isEnabled() == false || (principal.getId() != organizerId &&
        principal.getAuthorities().stream().anyMatch(r -> "Admin".equals(r.getAuthority()))))
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);


        Page<Event> eventPage = eventService.getEventByStatus(statusName,organizerId,p);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }

    //Missing admin search by status or id method

    @RequestMapping(value = "events/search", method = RequestMethod.POST)
    public ResponseEntity<PageEntity<Event>> search(@RequestBody EventSearchForm form,
                                                    @PageableDefault Pageable p){
        //get searched categories
        final Set<Category> categorySet = new HashSet<>();
        Set<Category> categories = categoryService.getAll();
        Map<Long, Category> categoryMap = categories.stream().collect(Collectors.toMap(Category::getId, c-> c));
        if(form.categories != null){
            form.categories.stream().forEach(id -> categorySet.add((Category) categoryMap.get(id)));
        }

        Page<Event> eventPage = eventService.searchEvent(form.title,categorySet,form.organizers,
                form.tags, form.startDate,form.endDate, p);
        return new ResponseEntity<>(new PageEntity<>(eventPage), HttpStatus.OK);
    }




    public static class eventIdListForm{
        public List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public eventIdListForm() {
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
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

        @Size(max = 50, message = "Title < 50")
        String title;

        @Size(max = 4000, message = "Content < 4000")
        String content;
        Set<String> tags;
        Set<String> organizerNames;

        @NotNull(message = "Must specify if event is online")
        boolean online;

        @NotNull(message = "Must specify startDate")
        Instant startDate;
        Instant endDate;

        @Size(max = 255, message = "Summary < 255")
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

    public static class EventSearchForm{
        public String title;
        public Set<String> tags;
        public Set<String> organizers;
        public Instant startDate;
        public Instant endDate;
        public Set<Long> categories;

        public EventSearchForm() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public Set<String> getOrganizers() {
            return organizers;
        }

        public void setOrganizers(Set<String> organizers) {
            this.organizers = organizers;
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

        public Set<Long> getCategories() {
            return categories;
        }

        public void setCategories(Set<Long> categories) {
            this.categories = categories;
        }
    }
}
