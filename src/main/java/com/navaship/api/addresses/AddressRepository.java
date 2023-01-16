package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends PagingAndSortingRepository<Address, Long> {
    Page<Address> findAllByUser(AppUser user, Pageable pageable);
    int countByUser(AppUser user);
    // findByUserAndByStreet1ContainingIgnoreCase
    List<Address> findByUserAndStreet1ContainingIgnoreCase(AppUser user, String street1, Pageable pageable);
}
