package com.olive.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.olive.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String fullName;

    private String email;

    @JsonIgnore
    private String password;

    // No roles/authorities for simplicity, but you can extend this
    // private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String fullName, String email, String password) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        // this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        // For simplicity, no roles are assigned. If you have roles, convert them to GrantedAuthority here.
        return new UserDetailsImpl(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return an empty list for now if no roles are implemented.
        return new java.util.ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() { // Spring Security uses this for the username (in our case, email)
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
