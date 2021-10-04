package com.wrox.site.repositories;

import com.wrox.site.entities.EventFeedback;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventFeedbackRepository extends PagingAndSortingRepository<EventFeedback, Long> {
}
