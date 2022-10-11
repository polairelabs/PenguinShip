package com.navaship.api.addresses;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Addresses {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String street1;
    private String street2;
    private String city;
    private String region; // state/province/region
    private String postalCode;
    private String country;
    private String residential;
    private String name;
    private String company;
    private String phone;
    private String email;
    private Boolean verified;
}
