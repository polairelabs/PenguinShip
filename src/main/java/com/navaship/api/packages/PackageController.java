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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/packages")
public class PackageController {
    private final PackageService packageService;
    private final AppUserService appUserService;


    @GetMapping
    public ResponseEntity<List<Package>> getAllPackagesForUser(JwtAuthenticationToken principal) {
        AppUser user = retrieveUserFromJwt(principal);
        return new ResponseEntity<>(packageService.getAllPackages(user), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Package> addPackage(JwtAuthenticationToken principal,
                                              @Valid @RequestBody PackageRequest parcel) {
        AppUser user = retrieveUserFromJwt(principal);
        return new ResponseEntity<>(packageService.savePackage(packageService.convertToPackage(parcel), user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Package> updatePackage(JwtAuthenticationToken principal,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody PackageRequest updatedParcel) {
        Package parcel = packageService.retrievePackage(id);
        checkResourceBelongsToUser(principal, parcel);
        return new ResponseEntity<>(packageService.modifyPackage(packageService.convertToPackage(updatedParcel)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Package> deletePackage(JwtAuthenticationToken principal,
                                                 @PathVariable Long id) {
        Package parcel = packageService.retrievePackage(id);
        checkResourceBelongsToUser(principal, parcel);
        return new ResponseEntity<>(packageService.deletePackage(parcel), HttpStatus.ACCEPTED);
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
