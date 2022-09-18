package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.appuser.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PackagesServices {

    private final PackagesRepository repository;
    private final AppUserService appUserService;

    @Autowired
    public PackagesServices(PackagesRepository repository, AppUserService appUserService){
        this.repository = repository;
        this.appUserService = appUserService;
    }


    public Packages savePackages(Packages packages, Long clientId) {
        // TODO: ADD VALIDATION HERE, KINDA BS
        AppUser appUser = appUserService.loadUserById(clientId);
        packages.setAppUser(appUser);
        return repository.save(packages);
    }

    public List<Packages> getPackagesForClient(Long clientId) {
        AppUser appUser = appUserService.loadUserById(clientId);
        return repository.findAllByAppUser(appUser);
    }

    public Packages modifyPackages(Packages packages, Long id) {
        Optional<Packages> optionalPackages= repository.findById(id);
        return repository.save(packages);
    }

    public Packages deletePackages(Long id) {
        return null;
    }

    public List<Packages> getPackages() {
        return repository.findAll();
    }
}
