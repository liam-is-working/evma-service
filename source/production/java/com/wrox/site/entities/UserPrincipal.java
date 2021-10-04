package com.wrox.site.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Entity
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UserPrincipal implements UserDetails, CredentialsContainer, Cloneable{

    private long id;
    private String username;
    private byte[] hashedPassword;
    private Set<UserAuthority> authorities = new HashSet<>();
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;


    public UserPrincipal() {
    }

    @Id
    @Column(name = "UserPrincipalId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }


    @Override
    @JsonProperty
    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(name = "HashedPassword")
    public byte[] getHashedPassword()
    {
        return this.hashedPassword;
    }

    public void setHashedPassword(byte[] password)
    {
        this.hashedPassword = password;
    }

    @Transient
    @Override
    public String getPassword()
    {
        return this.getHashedPassword() == null ? null :
                new String(this.getHashedPassword(), StandardCharsets.UTF_8);
    }

    @Override
    public void eraseCredentials()
    {
        this.hashedPassword = null;
    }

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinTable(name = "UserPrincipal_UserAuthority",
            joinColumns = {@JoinColumn(name = "UserPrincipalId")},
            inverseJoinColumns ={@JoinColumn(name = "UserAuthorityId")}
    )
    @JsonProperty
    public Set<UserAuthority> getAuthorities()
    {
        return this.authorities;
    }

    public void setAuthorities(Set<UserAuthority> authorities)
    {
        this.authorities = authorities;
    }

    @Override
    @JsonProperty
    public boolean isAccountNonExpired()
    {
        return this.accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired)
    {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    @JsonProperty
    public boolean isAccountNonLocked()
    {
        return this.accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked)
    {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired)
    {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    @JsonProperty
    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public int hashCode()
    {
        return this.username.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof UserPrincipal &&
                ((UserPrincipal)other).id == this.id;
    }

    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    protected UserPrincipal clone()
    {
        try {
            return (UserPrincipal)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // not possible
        }
    }

    @Override
    public String toString()
    {
        return this.username;
    }

    public void setAuthenticated(boolean b) {
    }
}
