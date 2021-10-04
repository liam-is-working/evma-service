package com.wrox.site.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UserAuthority implements GrantedAuthority {
    private String authority;
    private int id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserAuthorityId")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserAuthority() { }

    public UserAuthority(String authority)
    {
        this.authority = authority;
    }

    @Override
    @JsonProperty
    public String getAuthority()
    {
        return this.authority;
    }

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }
}
