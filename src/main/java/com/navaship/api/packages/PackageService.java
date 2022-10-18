package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PackageService {
    private final PackageRepository packageRepository;


    public Package savePackage(Package parcel, AppUser user) {
        parcel.setUser(user);
        return packageRepository.save(parcel);
    }

    public Optional<Package> findById(Long id) {
        return packageRepository.findById(id);
    }

    public List<Package> getAllPackages(AppUser user) {
        return packageRepository.findAllByUser(user);
    }

    public Package modifyPackage(Package parcel) {
        return packageRepository.save(parcel);
    }

    public Package deletePackage(Package parcel) {
        packageRepository.delete(parcel);
        return parcel;
    }

    public Package retrievePackage(Long parcelId) {
        return packageRepository.findById(parcelId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcel not found")
        );
    }
}
