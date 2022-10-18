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
    @Column(name = "id")
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
    private String residential;
    @Column(nullable = false)
    private String name; // name of the person or company
    private String company;
    private String phone;
    private String email;
    private Boolean verified;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private AppUser user;


    public Map<String, Object> toAddressMap() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", name);
        addressMap.put("street1", street1);
        addressMap.put("street2", street2);
        addressMap.put("city", city);
        addressMap.put("state", state);
        addressMap.put("country", country);
        addressMap.put("zip", zip);
        return addressMap;
    }
}
