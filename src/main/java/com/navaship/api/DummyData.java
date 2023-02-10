package com.navaship.api;

import com.navaship.api.address.Address;
import com.navaship.api.address.AddressRepository;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.appuser.AppUserRepository;
import com.navaship.api.appuser.AppUserRole;
import com.navaship.api.packages.Package;
import com.navaship.api.packages.PackageRepository;
import com.navaship.api.security.PasswordEncoder;
import com.navaship.api.subscriptiondetail.SubscriptionDetailRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
public class DummyData implements CommandLineRunner {
    private AppUserRepository appUserRepository;
    private AddressRepository addressRepository;
    private PackageRepository packageRepository;
    private SubscriptionDetailRepository subscriptionDetailRepository;
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) {
        AppUser admin = new AppUser("admin", "admin", "admin@lol.com", passwordEncoder.bCryptPasswordEncoder().encode("admin123"), "5146662222", "New york", "NY", "899 road", AppUserRole.ADMIN);
        appUserRepository.save(admin);
        Address address1 = new Address();
        address1.setStreet1("417 MONTGOMERY ST STE 500");
        address1.setCity("SAN FRANCISCO");
        address1.setState("CA");
        address1.setZip("94104");
        address1.setCountry("US");
        address1.setResidential(false);
        address1.setUser(admin);
        addressRepository.save(address1);
        Address address2 = new Address();
        address2.setStreet1("181 Fremont St");
        address2.setCity("SAN FRANCISCO");
        address2.setState("CA");
        address2.setZip("94105");
        address2.setCountry("US");
        address2.setResidential(false);
        address2.setUser(admin);
        addressRepository.save(address2);
        Package parcel = new Package();
        parcel.setName("My cool package");
        parcel.setWeight(BigDecimal.valueOf(12));
        parcel.setLength(BigDecimal.valueOf(12));
        parcel.setWidth(BigDecimal.valueOf(12));
        parcel.setHeight(BigDecimal.valueOf(10));
        parcel.setUser(admin);
        packageRepository.save(parcel);
    }
}