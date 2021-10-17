package com.wrox.site.services;

import com.wrox.site.SearchCriteria;
import com.wrox.site.entities.Category;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.EventStatus;
import com.wrox.site.repositories.CategoryRepository;
import com.wrox.site.repositories.EventRepository;
import com.wrox.site.repositories.EventStatusRepository;
import com.wrox.site.repositories.UserProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DefaultEventService implements EventService{

    @Inject
    EventRepository events;
    @Inject
    CategoryRepository category;
    @Inject
    EventStatusRepository status;
    @Inject
    UserProfileRepository profile;

    @Override
    @Transactional
    public Page<Event> getPublishedEvent(Pageable page) {
        return events.getEventByStatus(status.findEventStatusByName("Published"), page);
    }

    @Override
    @Transactional
    public Event getEventDetail(long eventId) {
        return events.findOne(eventId);
    }

    @Override
    @Transactional
    public Event saveEvent(Event event, Set<Long> categoryIds, int statusId) {
        Set<Category> categories = new HashSet<>();
        if(categoryIds!=null){
            for (long id : categoryIds){
                Category cat = category.findOne(id);
                if(cat!=null)
                    categories.add(cat);
            }
        }

        if(categories.size() != 0){
            event.setCategories(categories);
        }

        EventStatus eventStatus = status.findOne(statusId);
        event.setStatus(eventStatus);
        events.save(event);
        if(event.getCoverURL() == null){
            event.setCoverURL("EventCover_"+event.getId());
        }
        return event;
    }

    @Override
    @Transactional
    public Page<Event> getPublishedEvent(long ownerId, Pageable p) {
        return events.getEventByStatusAndUserProfileId(status.findEventStatusByName("Published"),ownerId, p);
    }

    @Override
    public Page<Event> getEventByStatus(String statusName,long ownerId, Pageable p) {
        EventStatus s = status.findEventStatusByName(statusName);
        if(s == null)
            throw new DataIntegrityViolationException("");

        return events.getEventByStatusAndUserProfileId(s,ownerId,p);
    }

    @Override
    public Page<Event> searchEvent(SearchCriteria criteria, Pageable p) {
        return events.searchEvent(criteria, p);
    }

    @Override
    public Page<Event> searchEvent(String title, Set<Category> categorySet, Set<String> nameSet,
                                   Set<String> tagSet, Instant startDate, Instant endDate, Pageable p) {

        return events.searchEvent(title, categorySet, nameSet, tagSet, startDate
                , endDate, p,status.findEventStatusByName("Published"));
    }

    @Override
    public Page<Event> getFollowedEvent(List<Long> ids, Pageable p) {
        return events.getEventByIdInAndStatusIsNot(ids, status.findEventStatusByName("Deleted"), p );
    }


}
