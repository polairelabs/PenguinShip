package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.shipments.NavaShipment;
import com.navaship.api.shipments.ShipmentRepository;
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
    private ShipmentRepository shipmentRepository;
    private ModelMapper modelMapper;

    public Package savePackage(Package parcel, AppUser user) {
        parcel.setUser(user);
        return packageRepository.save(parcel);
    }

    public List<Package> findAllPackages(AppUser user) {
        return packageRepository.findAllByUser(user);
    }

    public Package modifyPackage(Package parcel) {
        return packageRepository.save(parcel);
    }

    public void deletePackage(Package parcel) {
        // Set parcel to null to all shipments that used that parcel
        for (NavaShipment shipment : parcel.getShipments()) {
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
