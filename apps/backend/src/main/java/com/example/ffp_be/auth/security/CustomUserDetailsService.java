package com.example.ffp_be.auth.security;

import com.example.ffp_be.auth.entity.UserCredential;
import com.example.ffp_be.auth.exception.UserNotFoundException;
import com.example.ffp_be.auth.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialRepository userCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredential user = userCredentialRepository.findByEmail(username)
            .orElseThrow(() -> new UserNotFoundException(username + " > 찾을 수 없습니다."));
        return new CustomUserDetails(user);
    }
}
