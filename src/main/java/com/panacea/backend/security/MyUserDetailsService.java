package com.panacea.backend.security;


import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Entity.UserPrincipal;
import com.panacea.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + email));

        if(user==null){
            System.out.println("Login attempt for: " + email);
            throw new UsernameNotFoundException("User 404");
        }

        return new UserPrincipal(user);
    }
}
