package com.navaship.api.refreshtoken;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.jwt.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/auth")
public class RefreshTokenController {
    private RefreshTokenService refreshTokenService;
    private JwtService jwtService;


    @PostMapping("/refreshtoken")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        // Client exchanges refresh token to get a new access token and a new refresh token
        // Refresh token rotation is used to always provide the user with a new refresh token when he requests new access token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest.getToken()).orElseThrow(
                () -> new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token cannot be processed")
        );

        if (refreshTokenService.validateExpiration(refreshToken)) {
            throw new RefreshTokenException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        refreshTokenService.delete(refreshToken);
        AppUser user = refreshToken.getUser();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new RefreshTokenResponse(
                jwtService.createJwtAccessToken(authentication, user),
                refreshTokenService.createRefreshToken(user).getToken(),
                "Bearer"
        ));
    }
}
