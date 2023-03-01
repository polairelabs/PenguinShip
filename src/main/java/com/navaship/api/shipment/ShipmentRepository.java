package com.navaship.api.shipment;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends PagingAndSortingRepository<Shipment, Long> {
    Page<Shipment> findAllByUser(AppUser user, Pageable pageable);
    Optional<Shipment> findShipmentByEasypostShipmentId(String easypostShipmentId);
    int countByUser(AppUser user);
    int countByUserAndStatus(AppUser user, ShipmentStatus status);
}
