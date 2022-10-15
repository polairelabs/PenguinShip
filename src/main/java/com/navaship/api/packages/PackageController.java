package com.navaship.api.packages;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "apps/packages")
public class PackageController {

    private PackageService packageService;
    @Autowired
    public PackageController(PackageService packageService){
        this.packageService = packageService;
    }

    /*@GetMapping
    public ResponseEntity<List<Packages>> findAllPackages(){
        return new ResponseEntity<>(packagesServices.getPackages(), HttpStatus.OK);
    }*/

    @JsonView(PackagesView.Default.class)
    @PostMapping
    public ResponseEntity<Package> addPackages(@RequestBody Package aPackage, @RequestParam Long clientId) {
        return new ResponseEntity<>(packageService.savePackages(aPackage, clientId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Package>> findAllPackagesForClient(@RequestParam Long clientId){
        return new ResponseEntity<>(packageService.getPackagesForClient(clientId), HttpStatus.OK);
    }

    @JsonView(PackagesView.Default.class)
    @PutMapping("/{id}")
    public ResponseEntity<Package> updatePackages(@RequestBody Package evaluation, @PathVariable Long id) {
        return new ResponseEntity<>(packageService.modifyPackages(evaluation, id), HttpStatus.OK);
    }

    @JsonView(PackagesView.Default.class)
    @DeleteMapping("/{id}")
    @CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
    public ResponseEntity<Package> deletePackages(@PathVariable Long id) {
        return new ResponseEntity<>(packageService.deletePackages(id), HttpStatus.ACCEPTED);
    }

}
