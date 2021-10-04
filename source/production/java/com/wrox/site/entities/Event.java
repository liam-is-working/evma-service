package com.wrox.site.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrox.site.converters.InstantConverter;
import com.wrox.site.validation.Name;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Event implements Serializable {
    private long id;
    @Size(max = 50)
    private String title;

    private UserProfile userProfile;

    private Set<Category> categories;

    private Set<String> tags;

    private Set<String> organizerNames;

    private long userProfileId;

    @NotNull
    private boolean online;

//    private Set<Address> addresses;

    private Instant startDate;
    private Instant endDate;

    @NotNull
    private EventStatus status;

    private String coverURL;
    @Size(max = 140)
    private String summary;
    @Size(max = 2500)
    private String content;

    public Event(){}

    @Id
    @Column(name = "EventId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonProperty
    public long getUserProfileId() {
        return userProfileId;
    }

    public void setUserProfileId(long userProfileId) {
        this.userProfileId = userProfileId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserProfileId", referencedColumnName = "UserProfileId")
    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @JsonProperty
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "Event_Category",
            joinColumns = {@JoinColumn(name = "EventId")},
            inverseJoinColumns = {@JoinColumn(name = "CategoryId")})
    @JsonProperty
    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "Event_Tag",
            joinColumns = {@JoinColumn(name = "EventId", referencedColumnName = "EventId")})
    @Column(name = "Tag")
    @JsonProperty
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "Event_OrganizerName",
            joinColumns = {@JoinColumn(name = "EventId")})
    @Column(name = "OrganizerName")
    @JsonProperty
    public Set<String> getOrganizerNames() {
        return organizerNames;
    }

    public void setOrganizerNames(Set<String> organizerNames) {
        this.organizerNames = organizerNames;
    }

    @Basic(fetch = FetchType.EAGER)
    @JsonProperty
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }


//    @ElementCollection(fetch = FetchType.EAGER)
//    @CollectionTable(name = "Event_Address",
//            joinColumns = {@JoinColumn(name = "EventId", referencedColumnName = "EventId")})
//    @JsonProperty
//    public Set<Address> getAddresses() {
//        return addresses;
//    }
//
//    public void setAddresses(Set<Address> addresses) {
//        this.addresses = addresses;
//    }

    @Convert(converter = InstantConverter.class)
    @Basic(fetch = FetchType.EAGER)
    @JsonProperty
    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    @Convert(converter = InstantConverter.class)
    @Basic(fetch = FetchType.EAGER)
    @JsonProperty
    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "EventStatusId")
    @JsonProperty
    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    @JsonProperty
    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    @JsonProperty
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Basic(fetch = FetchType.EAGER)
    @JsonProperty
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}