package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface PackagesRepository extends JpaRepository<Packages, Long> {
    List<Packages> findAllByAppUser(AppUser user);
}
