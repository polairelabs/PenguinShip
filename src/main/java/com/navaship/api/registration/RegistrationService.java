package com.navaship.api.registration;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import com.navaship.api.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final AppUserService appUserService;


    public AppUser register(RegistrationRequest request) {
        return appUserService.createUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );
    }
}
