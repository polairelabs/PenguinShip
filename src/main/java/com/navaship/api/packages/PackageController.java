package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import com.navaship.api.verificationtoken.VerificationTokenException;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<PackageResponse>> getAllUserPackages(JwtAuthenticationToken principal) {
        AppUser user = retrieveUserFromJwt(principal);
        List<PackageResponse> parcels = packageService.findAllPackages(user)
                .stream().map(packageService::convertToPackagesResponse)
                .toList();
        return new ResponseEntity<>(parcels, HttpStatus.OK);
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
