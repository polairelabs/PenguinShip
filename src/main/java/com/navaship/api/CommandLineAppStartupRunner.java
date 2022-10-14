package com.navaship.api;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.appuser.AppUserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private final AppUserRepository appUserRepository;

    @Override
    public void run(String...args) {
        // Create default admin user
        AppUser admin = new AppUser();
        admin.setFirstName("Admin");
        admin.setLastName("Sir");
        admin.setPassword("admin");
        admin.setEmail("admin@lol.com");
        admin.setRole(AppUserRole.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);
    }
}