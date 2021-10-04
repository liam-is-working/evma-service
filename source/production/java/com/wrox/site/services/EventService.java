package com.wrox.site.services;

import com.wrox.site.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface EventService {
    public Page<Event> getEvents(Pageable page);
    public Event getEventDetail(long eventId);
    public Event saveEvent(Event event,long ownerId, Set<Long> categories, int status);
    public Page<Event> getEvents(long ownerId, Pageable p);
}
