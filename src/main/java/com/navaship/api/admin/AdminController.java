package com.navaship.api.admin;

import com.navaship.api.appuser.AppUserService;
import com.navaship.api.subscription.SubscriptionPlanRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/admin")
public class AdminController {
    private AppUserService appUserService;


    @PutMapping("/subscriptions")
    public ResponseEntity<String> updateSubscriptionPlan(JwtAuthenticationToken principal,
                                                         @RequestParam String subscriptionId,
                                                         @RequestBody SubscriptionPlanRequest subscriptionPlanRequest) {
        // verifyUserAdminStatus(principal);
        return new ResponseEntity<>("Yoo!", HttpStatus.OK);
    }

    //    private void verifyUserAdminStatus(JwtAuthenticationToken principal) {
    //        Long userId = (Long) principal.getTokenAttributes().get("id");
    //        AppUser user = appUserService.findById(userId)
    //                .orElseThrow(() -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found"));
    //        if (user.getRole() != AppUserRole.ADMIN) {
    //            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin");
    //        }
    //    }
}
