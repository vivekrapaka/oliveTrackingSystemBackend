package com.olive.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.olive.model.Role;
import com.olive.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private List<Long> projectIds;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String fullName, String email, String password,
                           Role role, List<Long> projectIds,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.projectIds = (projectIds != null) ? new ArrayList<>(projectIds) : new ArrayList<>();
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        String functionalGroup = user.getRole().getFunctionalGroup();
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + functionalGroup.toUpperCase()));

        return new UserDetailsImpl(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                (user.getProjectIds() != null) ? new ArrayList<>(user.getProjectIds()) : new ArrayList<>(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRoleTitle() { return role.getTitle(); }
    public String getFunctionalGroup() { return role.getFunctionalGroup(); }
    public List<Long> getProjectIds() { return projectIds; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}