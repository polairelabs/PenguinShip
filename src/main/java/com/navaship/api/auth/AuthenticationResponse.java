package com.navaship.api.auth;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class AuthenticationResponse {
    private String accessToken;
    private AppUser userData;
}
