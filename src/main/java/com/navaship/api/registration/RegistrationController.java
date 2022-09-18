package com.navaship.api.registration;

import com.navaship.api.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/register")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class RegistrationController {

    private RegistrationService registrationService;

    @PostMapping
    public AppUser register(@RequestBody RegistrationRequest request) {
        // TODO Check for valid email
        return registrationService.register(request);
    }
}