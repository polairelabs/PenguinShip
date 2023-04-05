package com.navaship.api.auth;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.auth.AuthViews;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
    @JsonView(AuthViews.Default.class)
    private AppUser user;
}
