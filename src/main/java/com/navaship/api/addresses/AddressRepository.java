package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUser(AppUser user);
}
