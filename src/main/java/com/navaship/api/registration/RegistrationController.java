package com.navaship.api.registration;

import com.navaship.api.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/register")
@AllArgsConstructor
public class RegistrationController {

    private RegistrationService registrationService;

    @PostMapping
    public AppUser register(@RequestBody RegistrationRequest request) {
        // TODO Check for valid email
        return registrationService.register(request);
    }
}