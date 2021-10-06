package com.wrox.site.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrox.site.converters.InstantConverter;
import com.wrox.site.validation.*;
import com.wrox.site.validation.Address;
import org.springframework.web.bind.annotation.Mapping;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@Entity
public class UserProfile implements Serializable {

    private long id;
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;

    //public List<OrganizerFeedback> organizerFeedbacks;

    @Email
    @NotBlank
    private String email;

    private Instant DOB;

    @Size(min =1, max = 50)
    @Name
    private String city;

    @Size(min = 1, max = 25)
    @Name
    private String jobTitle;

    @Size(min = 1, max = 50)
    @Address
    private String address;

    @PhoneNumber
    private String phoneNumber;

    @Size(max = 250)
    private String summary;

    private String avatarURL;

    private String backgroundURL;

    private UserAuthority role;


    public UserProfile() {
    }

    //get set
    @Id
    @Column(name = "UserProfileId")
    @JsonProperty
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserAuthorityId")
    @JsonProperty
    public UserAuthority getRole() {
        return role;
    }

    public void setRole(UserAuthority role) {
        this.role = role;
    }

    @Basic
    @JsonProperty
    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    @Basic
    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @JsonProperty
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Basic
    @JsonProperty
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @JsonProperty
    @Convert(converter = InstantConverter.class)
    public Instant getDOB() {
        return DOB;
    }

    public void setDOB(Instant DOB) {
        this.DOB = DOB;
    }

    @Basic
    @JsonProperty
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Basic
    @JsonProperty
    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    @Basic
    @JsonProperty
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Basic
    @JsonProperty
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Basic
    @JsonProperty
    public String getBackgroundURL() {
        return backgroundURL;
    }

    public void setBackgroundURL(String backgroundURL) {
        this.backgroundURL = backgroundURL;
    }

}

