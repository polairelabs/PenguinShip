package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class PackageService {
    private PackageRepository packageRepository;
    private ModelMapper modelMapper;

    public Package savePackage(Package parcel, AppUser user) {
        parcel.setUser(user);
        return packageRepository.save(parcel);
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

    public Package convertToPackage(PackageRequest packageRequest) {
        return modelMapper.map(packageRequest, Package.class);
    }

    public PackageResponse convertToPackagesResponse(Package parcel) {
        return modelMapper.map(parcel, PackageResponse.class);
    }
}
