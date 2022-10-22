package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.packages.Package;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/addresses")
public class AddressController {
    private final AddressService addressService;
    private final AppUserService appUserService;


    @GetMapping
    public ResponseEntity<List<Address>> getAllAddressesForUser(JwtAuthenticationToken principal) {
        AppUser user = retrieveUserFromJwt(principal);
        return new ResponseEntity<>(addressService.getAllAddresses(user), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(JwtAuthenticationToken principal,
                                              @Valid @RequestBody Address newAddress) {
        AppUser user = retrieveUserFromJwt(principal);
        Address address = addressService.saveAddress(newAddress, user);
        return new ResponseEntity<>(new AddressResponse(address), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Address> updateAddress(JwtAuthenticationToken principal,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody Address updatedAddress) {
        Address address = addressService.retrieveAddress(id);
        checkResourceBelongsToUser(principal, address);
        return new ResponseEntity<>(addressService.modifyAddress(updatedAddress), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Address> deletePackage(JwtAuthenticationToken principal,
                                                 @PathVariable Long id) {
        Address address = addressService.retrieveAddress(id);
        checkResourceBelongsToUser(principal, address);
        return new ResponseEntity<>(addressService.deleteAddress(address), HttpStatus.ACCEPTED);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }

    private void checkResourceBelongsToUser(JwtAuthenticationToken principal,
                                            Address address) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        if (!address.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }
}
