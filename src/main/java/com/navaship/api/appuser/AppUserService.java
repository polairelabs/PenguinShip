package com.navaship.api.appuser;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {
    private AppUserRepository appUserRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Incorrect login details"));
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public Optional<AppUser> findById(UUID id) {
        return appUserRepository.findById(id);
    }

    public AppUser createUser(AppUser user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        appUserRepository.save(user);

        return user;
    }

    public AppUser updateUser(AppUser user) {
        return appUserRepository.save(user);
    }

    public void enableUserAccount(AppUser user) {
        user.setEnabled(true);
        appUserRepository.save(user);
    }

    public void changePassword(AppUser user, String password) {
        user.setPassword(bCryptPasswordEncoder.encode(password));
        appUserRepository.save(user);
    }

    public int countAdminUsers() {
        return appUserRepository.countByRole(AppUserRoleEnum.ADMIN);
    }
}
