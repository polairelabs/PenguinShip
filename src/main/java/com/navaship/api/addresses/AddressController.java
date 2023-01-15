package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.common.ListApiResponse;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.navaship.api.common.ListApiConstants.*;

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
    public ResponseEntity<ListApiResponse<AddressResponse>> getAllUserAddresses(JwtAuthenticationToken principal,
                                                                                 @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NUMBER + "") int pageNumber,
                                                                                 @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE + "") int pageSize,
                                                                                 @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_FIELD) String sortField,
                                                                                 @RequestParam(value = "order", defaultValue = DEFAULT_DIRECTION) String sortDirection) {
        AppUser user = retrieveUserFromJwt(principal);
        ListApiResponse<AddressResponse> listApiResponse = new ListApiResponse<>();

        // Validate page size
        if (pageSize > DEFAULT_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Decrement page number to match zero-based index
        int zeroBasedPageNumber = pageNumber - 1;

        try {
            // Retrieve addresses with pagination
            Page<Address> addressesWithPagination = addressService.findAllAddresses(user, zeroBasedPageNumber, pageSize, sortField, Sort.Direction.valueOf(sortDirection.toUpperCase()));
            listApiResponse.setData(addressesWithPagination.map(addressService::convertToAddressResponse).toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        // Calculate total pages
        int totalPages = (int) Math.round(addressService.retrieveUserAddressesCount(user) / (double) pageSize);
        listApiResponse.setTotalPages(totalPages);
        listApiResponse.setCount(listApiResponse.getData().size());
        listApiResponse.setCurrentPage(zeroBasedPageNumber + 1);

        return new ResponseEntity<>(listApiResponse, HttpStatus.OK);
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
        convertedAddress.setId(addressId);
        convertedAddress.setUser(user);
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

        addressService.deleteAddress(address);
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
