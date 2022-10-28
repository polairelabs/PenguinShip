package com.navaship.api.addresses;

import com.navaship.api.appuser.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String street1;
    private String street2;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state; // state or province
    @Column(nullable = false)
    private String zip; // zip or postal code
    @Column(nullable = false)
    private String country;
    private Boolean residential;
    @ManyToOne
    private AppUser user;

    // Info to reach the person/organization (The more information, the better)
    private String name;
    private String company;
    private String phone;
    private String email;


    public Map<String, Object> toAddressMap() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("street1", street1);
        addressMap.put("street2", street2);
        addressMap.put("city", city);
        addressMap.put("state", state);
        addressMap.put("country", country);
        addressMap.put("zip", zip);
        addressMap.put("residential", residential);
        addressMap.put("name", name);
        addressMap.put("company", company);
        addressMap.put("phone", phone);
        addressMap.put("email", email);
        return addressMap;
    }
}
