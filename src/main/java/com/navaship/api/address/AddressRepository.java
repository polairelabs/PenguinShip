package com.navaship.api.address;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends PagingAndSortingRepository<Address, Long> {
    Page<Address> findAllByUser(AppUser user, Pageable pageable);
    int countByUser(AppUser user);
}
