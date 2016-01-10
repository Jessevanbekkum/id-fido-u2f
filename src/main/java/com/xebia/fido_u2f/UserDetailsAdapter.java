package com.xebia.fido_u2f;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsAdapter implements UserDetails {

    private User account;

    public UserDetailsAdapter(User account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_PRE_AUTH_USER");
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserDetailsAdapter{");
        sb.append("account=").append(account);
        sb.append('}');
        return sb.toString();
    }
}