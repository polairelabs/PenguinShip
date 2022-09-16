package com.navaship.api.packages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PackagesServices {

    private final PackagesRepository repository;

    @Autowired
    public PackagesServices(PackagesRepository repository){
        this.repository = repository;
    }


    public Packages savePackages(Packages packages, Long clientId) {
        return repository.save(packages);
    }

    public Set<Packages> getPackagesForClient(Long clientId) {
        return null;
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
