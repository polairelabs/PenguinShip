package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/addresses")
public class AddressController {
    private AddressService addressService;
    private AppUserService appUserService;


    @GetMapping("/{addressId}")
    public AddressResponse getAddressById(JwtAuthenticationToken principal,
                                          @PathVariable Long addressId) {
        Address address = addressService.retrieveAddress(addressId);
        checkResourceBelongsToUser(principal, address);
        return addressService.convertToAddressResponse(address);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddressesByUser(JwtAuthenticationToken principal) {
        AppUser user = retrieveUserFromJwt(principal);
        List<AddressResponse> addresses = addressService.findAllAddresses(user)
                .stream().map(addressService::convertToAddressResponse)
                .toList();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(JwtAuthenticationToken principal,
                                                      @Valid @RequestBody AddressRequest addressRequest) {
        AppUser user = retrieveUserFromJwt(principal);
        Address address = addressService.createAddress(addressService.convertToAddress(addressRequest), user);
        return new ResponseEntity<>(addressService.convertToAddressResponse(address), HttpStatus.CREATED);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(JwtAuthenticationToken principal,
                                                         @PathVariable Long addressId,
                                                         @Valid @RequestBody AddressRequest addressRequest) {
        AppUser user = retrieveUserFromJwt(principal);
        Address address = addressService.retrieveAddress(addressId);
        checkResourceBelongsToUser(principal, address);

        Address convertedAddress = addressService.convertToAddress(addressRequest);
        convertedAddress.setUser(user);
        convertedAddress.setId(addressId);
        convertedAddress.setCreatedAt(address.getCreatedAt());
        convertedAddress.setUpdatedAt(LocalDateTime.now());

        Address updatedAddress = addressService.modifyAddress(convertedAddress);
        return new ResponseEntity<>(addressService.convertToAddressResponse(updatedAddress), HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Map<String, String>> deleteAddress(JwtAuthenticationToken principal,
                                                             @PathVariable Long addressId) {
        Address address = addressService.retrieveAddress(addressId);
        checkResourceBelongsToUser(principal, address);

        addressService.deleteAddress(addressId);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Successfully deleted address %d", addressId));
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
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
