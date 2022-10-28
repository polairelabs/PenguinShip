package com.navaship.api.shipments;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ShipmentRepository extends JpaRepository<NavaShipment, Long> {
    List<NavaShipment> findAllByUser(AppUser user);
    Optional<NavaShipment> findShipmentByEasypostShipmentId(String easypostShipmentId);
}
