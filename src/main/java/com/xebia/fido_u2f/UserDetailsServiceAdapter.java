package com.xebia.fido_u2f;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Primary
public class UserDetailsServiceAdapter implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User account = accountRepository.getUser(username);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserDetailsAdapter(account);
    }
}
