package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.common.ListApiResponse;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/packages")
public class PackageController {
    private final PackageService packageService;
    private final AppUserService appUserService;


    @GetMapping("/{packageId}")
    public ResponseEntity<PackageResponse> getPackage(JwtAuthenticationToken principal, @PathVariable Long packageId) {
        Package parcel = packageService.retrievePackage(packageId);
        checkResourceBelongsToUser(principal, parcel);
        return new ResponseEntity<>(packageService.convertToPackagesResponse(parcel), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ListApiResponse<PackageResponse>> getAllUserPackages(JwtAuthenticationToken principal,
                                                                               @RequestParam(value = "offset", defaultValue = DEFAULT_PAGE_NUMBER + "") int offset,
                                                                               @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE + "") int pageSize,
                                                                               @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_FIELD) String sortField,
                                                                               @RequestParam(value = "order", defaultValue = DEFAULT_DIRECTION) String sortDirection) {
        AppUser user = retrieveUserFromJwt(principal);
        ListApiResponse<PackageResponse> listApiResponse = new ListApiResponse<>();

        if (pageSize > DEFAULT_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // Decrement page number to match zero-based index
        int zeroBasedPageNumber = offset - 1;

        try {
            Page<Package> addressesWithPagination = packageService.findAllPackages(user, zeroBasedPageNumber, pageSize, sortField, Sort.Direction.valueOf(sortDirection.toUpperCase()));
            listApiResponse.setData(addressesWithPagination.map(packageService::convertToPackagesResponse).toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        int totalCount = packageService.retrieveUserPackagesCount(user);
        int totalPages = (int) Math.round(totalCount / (double) pageSize);
        listApiResponse.setTotalCount(totalCount);
        listApiResponse.setTotalPages(totalPages);
        listApiResponse.setCurrentPage(zeroBasedPageNumber + 1);

        return new ResponseEntity<>(listApiResponse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PackageResponse> addPackage(JwtAuthenticationToken principal,
                                                      @Valid @RequestBody PackageRequest packageRequest) {
        AppUser user = retrieveUserFromJwt(principal);
        Package parcel = packageService.savePackage(packageService.convertToPackage(packageRequest), user);
        return new ResponseEntity<>(packageService.convertToPackagesResponse(parcel), HttpStatus.CREATED);
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<PackageResponse> updatePackage(JwtAuthenticationToken principal,
                                                         @PathVariable Long packageId,
                                                         @Valid @RequestBody PackageRequest packageRequest) {
        AppUser user = retrieveUserFromJwt(principal);
        Package parcel = packageService.retrievePackage(packageId);
        checkResourceBelongsToUser(principal, parcel);

        Package convertedPackage = packageService.convertToPackage(packageRequest);
        convertedPackage.setId(packageId);
        convertedPackage.setUser(user);
        convertedPackage.setCreatedAt(parcel.getCreatedAt());
        convertedPackage.setUpdatedAt(LocalDateTime.now());

        Package updatedPackage = packageService.modifyPackage(convertedPackage);
        return new ResponseEntity<>(packageService.convertToPackagesResponse(updatedPackage), HttpStatus.OK);
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Map<String, String>> deletePackage(JwtAuthenticationToken principal,
                                                             @PathVariable Long packageId) {
        Package parcel = packageService.retrievePackage(packageId);
        checkResourceBelongsToUser(principal, parcel);
        packageService.deletePackage(parcel);

        Map<String, String> message = new HashMap<>();
        message.put("message", String.format("Successfully deleted package %d", packageId));
        return new ResponseEntity<>(message, HttpStatus.ACCEPTED);
    }

    private AppUser retrieveUserFromJwt(JwtAuthenticationToken principal) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        return appUserService.findById(userId).orElseThrow(
                () -> new VerificationTokenException(HttpStatus.NOT_FOUND, "User not found")
        );
    }

    private void checkResourceBelongsToUser(JwtAuthenticationToken principal,
                                            Package parcel) {
        Long userId = (Long) principal.getTokenAttributes().get("id");
        if (!parcel.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access/modify resource");
        }
    }
}
