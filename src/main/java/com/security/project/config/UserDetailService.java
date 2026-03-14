package com.security.project.config;

import com.security.project.entity.User;
import com.security.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // Rolümüzü oluşturuyoruz
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        // Spring Security'nin beklediği gibi rolü bir List içerisine alarak (Collections.singletonList) gönderiyoruz
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPwd(),
                Collections.singletonList(authority)
        );
    }
}