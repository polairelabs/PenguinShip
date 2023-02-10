package com.navaship.api.shipmentaddress;

import com.navaship.api.address.Address;
import com.navaship.api.shipment.Shipment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Address will be saved as ShipmentAddress once Address is persisted in the db
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"shipment_id", "type"})})
public class ShipmentAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;
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
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipmentAddressType type;


    public ShipmentAddress(Address address, ShipmentAddressType type) {
        this.street1 = address.getStreet1();
        this.street2 = address.getStreet2();
        this.city = address.getCity();
        this.state = address.getState();
        this.zip = address.getZip();
        this.country = address.getCountry();
        this.residential = address.getResidential();
        this.type = type;
    }
}
