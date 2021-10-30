package com.wrox.site.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wrox.site.validation.Name;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@Entity
public class Category implements Serializable, Comparable<Category>{

    private long id;
    @Name
    private String name;
    private boolean status;

    public Category(){}

    @JsonProperty
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Id
    @Column(name = "CategoryId")
    @JsonProperty
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Category o) {
        return (this.id-o.getId())>0?0:-1;
    }
}
