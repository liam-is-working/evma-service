package com.wrox.site.entities;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "OrganizerFeedbackId"))
public class OrganizerFeedback extends FeedbackEntity {
}
