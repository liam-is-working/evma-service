package com.wrox.site.entities;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "EventFeedbackId"))
public class EventFeedback extends FeedbackEntity {
    public EventFeedback() {
    }
}
