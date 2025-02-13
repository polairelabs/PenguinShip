package com.navaship.api.address;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.common.ListApiResponse;
import com.navaship.api.jwt.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static com.navaship.api.common.ListApiConstants.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/addresses")
public class AddressController {
    private AddressService addressService;
    private JwtService jwtService;


    @GetMapping("/{addressId}")
    public AddressResponse getAddressById(JwtAuthenticationToken principal,
                                          @PathVariable Long addressId) {
        Address address = addressService.retrieveAddress(addressId);
        jwtService.checkResourceBelongsToUser(principal, address);
        return addressService.convertToAddressResponse(address);
    }

    @GetMapping
    public ResponseEntity<ListApiResponse<AddressResponse>> getAllUserAddresses(JwtAuthenticationToken principal,
                                                                                @RequestParam(value = "offset", defaultValue = DEFAULT_PAGE_NUMBER + "") int offset,
                                                                                @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE + "") int pageSize,
                                                                                @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_FIELD) String sortField,
                                                                                @RequestParam(value = "order", defaultValue = DEFAULT_DIRECTION) String sortDirection) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        ListApiResponse<AddressResponse> listApiResponse = new ListApiResponse<>();

        if (pageSize > DEFAULT_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Decrement page number to match zero-based index
        int zeroBasedPageNumber = offset - 1;

        try {
            Page<Address> addressesWithPagination = addressService.findAllAddresses(user, zeroBasedPageNumber, pageSize, sortField, Sort.Direction.valueOf(sortDirection.toUpperCase()));
            listApiResponse.setData(addressesWithPagination.map(addressService::convertToAddressResponse).toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        int totalCount = addressService.retrieveUserAddressesCount(user);
        int totalPages = (int) Math.round(totalCount / (double) pageSize);
        listApiResponse.setTotalCount(totalCount);
        listApiResponse.setTotalPages(totalPages);
        listApiResponse.setCurrentPage(zeroBasedPageNumber + 1);

        return new ResponseEntity<>(listApiResponse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(JwtAuthenticationToken principal,
                                                      @Valid @RequestBody AddressRequest addressRequest) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        Address address = addressService.createAddress(addressService.convertToAddress(addressRequest), user);
        return new ResponseEntity<>(addressService.convertToAddressResponse(address), HttpStatus.CREATED);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(JwtAuthenticationToken principal,
                                                         @PathVariable Long addressId,
                                                         @Valid @RequestBody AddressRequest addressRequest) {
        AppUser user = jwtService.retrieveUserFromJwt(principal);
        Address address = addressService.retrieveAddress(addressId);
        jwtService.checkResourceBelongsToUser(principal, address);

        Address convertedAddress = addressService.convertToAddress(addressRequest);
        convertedAddress.setId(address.getId());
        convertedAddress.setUser(user);
        convertedAddress.setCreatedAt(address.getCreatedAt());

        Address updatedAddress = addressService.updateAddress(convertedAddress);
        return new ResponseEntity<>(addressService.convertToAddressResponse(updatedAddress), HttpStatus.OK);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Map<String, String>> deleteAddress(JwtAuthenticationToken principal,
                                                             @PathVariable Long addressId) {
        Address address = addressService.retrieveAddress(addressId);
        jwtService.checkResourceBelongsToUser(principal, address);

        addressService.deleteAddress(address);
        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Successfully deleted address %d", addressId));
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }
}
