package com.wrox.site.repositories;


import com.wrox.site.SearchCriteria;
import com.wrox.site.entities.Category;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface CustomEventRepository {
    Page<Event> searchEvent(SearchCriteria criteria, Pageable p);
    Page<Event> searchEvent(String title, Set<Category> categorySet, Set<String> organizerNameSet,
                            Set<String> tagSet, Instant startDate, Instant endDate, Pageable p, EventStatus published);
    Page<Event> getEventByIds(Set<Long> ids);
}
