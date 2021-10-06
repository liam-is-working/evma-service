package com.wrox.site.services;

import com.wrox.site.entities.EventStatus;
import com.wrox.site.repositories.EventStatusRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultEventStatusService implements EventStatusService{
    @Inject
    EventStatusRepository statusRepository;
    @Override
    public EventStatus getStatus(String statusName) {
        return statusRepository.findEventStatusByName(statusName);
    }

    @Override
    public EventStatus getStatus(int statusId) {
        return statusRepository.findOne(statusId);
    }

    @Override
    public List<EventStatus> getStatus() {
        List<EventStatus> resultList = new ArrayList<>();
        for (EventStatus e: statusRepository.findAll()){
            resultList.add(e);
        }
        return resultList;
    }
}
