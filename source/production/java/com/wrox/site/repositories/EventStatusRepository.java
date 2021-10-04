package com.wrox.site.repositories;

import com.wrox.site.entities.EventStatus;
import org.springframework.data.repository.CrudRepository;

public interface EventStatusRepository extends CrudRepository<EventStatus, Integer> {
}
