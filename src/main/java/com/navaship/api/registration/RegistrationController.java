package com.navaship.api.registration;

import com.fasterxml.jackson.annotation.JsonView;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRole;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.auth.AuthViews;
import com.navaship.api.sendgrid.SendGridEmailService;
import com.navaship.api.stripe.StripeService;
import com.navaship.api.subscription.SubscriptionPlan;
import com.navaship.api.subscription.SubscriptionPlanService;
import com.navaship.api.subscriptiondetail.SubscriptionDetail;
import com.navaship.api.subscriptiondetail.SubscriptionDetailService;
import com.navaship.api.verificationtoken.VerificationToken;
import com.navaship.api.verificationtoken.VerificationTokenService;
import com.navaship.api.verificationtoken.VerificationTokenType;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1")
public class RegistrationController {
    private AppUserService appUserService;
    private VerificationTokenService verificationTokenService;
    private SendGridEmailService sendGridEmailService;
    private SubscriptionDetailService subscriptionDetailService;
    private SubscriptionPlanService subscriptionPlanService;
    private StripeService stripeService;


    // TODO: Add captcha
    @PostMapping("/register")
    @JsonView(AuthViews.Default.class)
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (appUserService.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser newUser = new AppUser(
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                registrationRequest.getEmail(),
                registrationRequest.getPassword(),
                registrationRequest.getPhoneNumber(),
                registrationRequest.getCity(),
                registrationRequest.getState(),
                registrationRequest.getAddress(),
                AppUserRole.UNPAYED_USER
        );

        SubscriptionPlan subscriptionPlan = subscriptionPlanService.retrieveSubscriptionPlan(registrationRequest.getStripePriceId());

        AppUser user = appUserService.createUser(newUser);
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(user, VerificationTokenType.VERIFY_EMAIL);
        sendGridEmailService.sendVerifyAccountEmail(user.getEmail(), verificationToken.getToken());

        RegistrationResponse registrationResponse = new RegistrationResponse();
        registrationResponse.setUser(user);

        try {
            Customer customer = stripeService.createCustomer(user);
            SubscriptionDetail subscriptionDetail = new SubscriptionDetail();
            subscriptionDetail.setStripeCustomerId(customer.getId());
            subscriptionDetail.setSubscriptionPlan(subscriptionPlan);
            subscriptionDetail.setUser(user);
            subscriptionDetailService.createSubscriptionDetail(subscriptionDetail);
            registrationResponse.setStripeCustomerId(subscriptionDetail.getStripeCustomerId());
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new ResponseEntity<>(registrationResponse, HttpStatus.CREATED);
    }
}
