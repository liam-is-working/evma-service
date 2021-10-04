package com.wrox.site.repositories;

import com.wrox.site.entities.OrganizerFeedback;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizerFeedbackRepository extends PagingAndSortingRepository<OrganizerFeedback, Long> {
}
