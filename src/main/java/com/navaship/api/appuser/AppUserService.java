package com.navaship.api.appuser;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Incorrect login details"));
    }

    public AppUser loadUserById(Long Id) throws UsernameNotFoundException {
        return appUserRepository.findById(Id).orElseThrow(() -> new UsernameNotFoundException("Incorrect login details"));
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public AppUser createUser(AppUser user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        appUserRepository.save(user);

        return user;
    }

    public void enableUserAccount(AppUser user) {
        user.setEnabled(true);
        appUserRepository.save(user);
    }

    public void changePassword(AppUser user, String password) {
        user.setPassword(bCryptPasswordEncoder.encode(password));
        appUserRepository.save(user);
    }
}
