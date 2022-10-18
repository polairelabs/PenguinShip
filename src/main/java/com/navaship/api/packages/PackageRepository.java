package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findAllByUser(AppUser user);
}
