package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackageService {

    private final PackageRepository repository;
    private final AppUserService appUserService;

    @Autowired
    public PackageService(PackageRepository repository, AppUserService appUserService){
        this.repository = repository;
        this.appUserService = appUserService;
    }


    public Package savePackages(Package aPackage, Long clientId) {
        // TODO: ADD VALIDATION HERE, KINDA BS
        AppUser user = appUserService.loadUserById(clientId);
        aPackage.setUser(user);
        return repository.save(aPackage);
    }

    public List<Package> getPackagesForClient(Long clientId) {
        AppUser user = appUserService.loadUserById(clientId);
        return repository.findAllByUser(user);
    }

    public Package modifyPackages(Package aPackage, Long id) {
        Optional<Package> optionalPackages = repository.findById(id);
        return repository.save(aPackage);
    }

    public Package deletePackages(Long id) {
        Optional<Package> optionalPackages = repository.findById(id);
        if(optionalPackages.isEmpty()){
            //TODO: Add exceptions for packages
            throw new UsernameNotFoundException("not found");
        }
        repository.delete(optionalPackages.get());
        return optionalPackages.get();
    }

    public List<Package> getPackages() {
        return repository.findAll();
    }
}
