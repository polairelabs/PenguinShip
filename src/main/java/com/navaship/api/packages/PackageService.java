package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.shipments.Shipment;
import com.navaship.api.shipments.ShipmentRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class PackageService {
    private PackageRepository packageRepository;
    private ShipmentRepository shipmentRepository;
    private ModelMapper modelMapper;

    public Package savePackage(Package parcel, AppUser user) {
        parcel.setUser(user);
        return packageRepository.save(parcel);
    }

    public Page<Package> findAllPackages(AppUser user, int pageNumber, int pageSize, String field, Sort.Direction direction) {
        return packageRepository.findAllByUser(user, PageRequest.of(pageNumber, pageSize).withSort(Sort.by(direction, field)));
    }

    public int retrieveUserPackagesCount(AppUser user) {
        return packageRepository.countByUser(user);
    }

    public Package modifyPackage(Package parcel) {
        return packageRepository.save(parcel);
    }

    public void deletePackage(Package parcel) {
        // Set parcel to null to all shipments that used that parcel
        for (Shipment shipment : parcel.getShipments()) {
            shipment.setParcel(null);
            shipmentRepository.save(shipment);
        }
        packageRepository.delete(parcel);
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
