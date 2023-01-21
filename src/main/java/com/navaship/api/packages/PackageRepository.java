package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    Page<Package> findAllByUser(AppUser user, Pageable pageable);
    int countByUser(AppUser user);
}
