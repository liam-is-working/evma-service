package com.wrox.site.services;

import com.wrox.site.entities.EventStatus;

import java.util.List;

public interface EventStatusService {
    EventStatus getStatus(String statusName);
    EventStatus getStatus(int statusId);
    List<EventStatus> getStatus();
}
