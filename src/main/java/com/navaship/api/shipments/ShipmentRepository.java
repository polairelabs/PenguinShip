package com.navaship.api.shipments;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ShipmentRepository extends PagingAndSortingRepository<NavaShipment, Long> {
    List<NavaShipment> findAllByUser(AppUser user);
    Page<NavaShipment> findAllByUser(AppUser user, Pageable pageable);
    Optional<NavaShipment> findShipmentByEasypostShipmentId(String easypostShipmentId);
}
