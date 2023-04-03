package com.navaship.api.shipment;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.easypost.EasyPostShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends PagingAndSortingRepository<Shipment, Long> {
    Page<Shipment> findAllByUser(AppUser user, Pageable pageable);
    Optional<Shipment> findShipmentByEasypostShipmentId(String easypostShipmentId);
    int countByUser(AppUser user);
    int countByUserAndStatus(AppUser user, ShipmentStatus status);
    int countByUserAndEasyPostStatus(AppUser user, EasyPostShipmentStatus easyPostStatus);
    Shipment findTopByUserOrderByCreatedAtDesc(AppUser user);
    @Query("SELECT SUM(s.rate.rate) FROM Shipment s WHERE s.user = :user")
    BigDecimal totalSumRateByUser(AppUser user);
    @Query("SELECT SUM(s.rate.retailRate) FROM Shipment s WHERE s.user = :user")
    BigDecimal totalSumRetailRate(AppUser user);
}
