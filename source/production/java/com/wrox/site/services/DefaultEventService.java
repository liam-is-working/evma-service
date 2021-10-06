package com.wrox.site.services;

import com.wrox.site.entities.Category;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.EventStatus;
import com.wrox.site.repositories.CategoryRepository;
import com.wrox.site.repositories.EventRepository;
import com.wrox.site.repositories.EventStatusRepository;
import com.wrox.site.repositories.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
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
    public Page<Event> getEvents(Pageable page) {
        return events.findAll(page);
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
        for (long id : categoryIds){
            categories.add(category.findOne(id));
        }
        EventStatus eventStatus = status.findOne(statusId);
        event.setCategories(categories);
        event.setStatus(eventStatus);
        events.save(event);
        if(event.getCoverURL() == null){
            event.setCoverURL("EventCover_"+event.getId());
        }
        return event;
    }

    @Override
    @Transactional
    public Page<Event> getEvents(long ownerId, Pageable p) {
        return events.getEventByUserProfileId(ownerId, p);
    }


}
