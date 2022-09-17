package com.navaship.api.packages;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "apps/packages")
public class PackagesController {

    private PackagesServices packagesServices;
    @Autowired
    public PackagesController(PackagesServices packagesServices){
        this.packagesServices = packagesServices;
    }

    @GetMapping
    public ResponseEntity<List<Packages>> findAllPackages(){
        return new ResponseEntity<>(packagesServices.getPackages(), HttpStatus.OK);
    }

    @JsonView(PackagesView.Default.class)
    @PostMapping
    public ResponseEntity<Packages> addPackages(@RequestBody Packages packages, @RequestParam Long clientId) {
        return new ResponseEntity<>(packagesServices.savePackages(packages, clientId), HttpStatus.CREATED);
    }

/*    @JsonView(PackagesView.StudentEvaluations.class)
    @GetMapping
    public ResponseEntity<Set<Packages>> findAllPackagesForClient(@RequestParam Long studentId){
        return new ResponseEntity<>(packagesServices.getPackagesForClient(studentId), HttpStatus.OK);
    }*/

    @JsonView(PackagesView.Default.class)
    @PutMapping("/{id}")
    public ResponseEntity<Packages> updatePackages( @RequestBody Packages evaluation, @PathVariable Long id) {
        return new ResponseEntity<>(packagesServices.modifyPackages(evaluation, id), HttpStatus.OK);
    }

    @JsonView(PackagesView.Default.class)
    @DeleteMapping("/{id}")
    public ResponseEntity<Packages> deletePackages(@PathVariable Long id) {
        return new ResponseEntity<>(packagesServices.deletePackages(id), HttpStatus.ACCEPTED);
    }

}
