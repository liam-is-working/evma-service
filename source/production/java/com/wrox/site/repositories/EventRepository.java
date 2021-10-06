package com.wrox.site.repositories;

import com.wrox.site.entities.Event;
import com.wrox.site.entities.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends PagingAndSortingRepository<Event, Long> {
     Page<Event> getEventByUserProfileId(long userProfileId, Pageable p);
}
