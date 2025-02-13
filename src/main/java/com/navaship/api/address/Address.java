package com.navaship.api.address;

import com.navaship.api.appuser.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    private String state;
    @Column(nullable = false)
    private String zip;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private Boolean residential;
    @ManyToOne
    private AppUser user;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Additional info to reach the person/organization (not saved in the database)
    @Transient
    private String name;
    @Transient
    private String company;
    @Transient
    private String phone;
    @Transient
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
