package com.wrox.site.services;

import com.wrox.site.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface EventService {
     Page<Event> getEvents(Pageable page);
     Event getEventDetail(long eventId);
     Event saveEvent(Event event, Set<Long> categories, int status);
     Page<Event> getEvents(long ownerId, Pageable p);
}
