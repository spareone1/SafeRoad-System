package com.teamroute.saferoad.security;

import com.teamroute.saferoad.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

// Spring Security가 인증/인가에 사용하는 User 객체
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // userstatus(Enum)를 기반으로 권한(Role) 부여

        String role;

        switch (user.getUserstatus()) {
            case admin_user:
                role = "ROLE_ADMIN";
                break;
            case normal_user:
                role = "ROLE_USER";
                break;
            default:
                role = "ROLE_USER";
        }

        return Collections.singleton(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPw();
    }

    @Override
    public String getUsername() {
        return user.getUserid();
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
        // userstatus가 deleted가 아닐 때만 활성화
        return !user.getUserstatus().equals(User.UserStatus.deleted);
    }
}