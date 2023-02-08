package com.navaship.api.shipments;

import com.easypost.model.Parcel;
import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import com.navaship.api.person.Person;
import com.navaship.api.rates.Rate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor
@Entity
@Table(name = "shipment")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "easypost_shipment_id", nullable = false)
    private String easypostShipmentId;
    @ManyToOne
    private AppUser user;

    // Contains both the source and destination addresses
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<ShipmentAddress> addresses = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private ShipmentPackage parcel;

    @OneToOne(cascade = CascadeType.ALL)
    private Rate rate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private ShipmentStatusEasyPost easypostShipmentStatusEasyPost; // Retrieved from easypost webhook

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<Person> persons = new ArrayList<>();

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Gets populated when a rate gets bought for the current shipment
    private String trackingCode;
    private String postageLabelUrl;
    private String publicTrackingUrl;


    // Once Shipment is saved/created, the parcel will be transformed into a ShipmentParcel as to never change the original Parcel
    public void setParcel(Package parcel) {
        this.parcel = new ShipmentPackage(parcel);
        this.parcel.setShipment(this);
    }

    // Once Shipment is saved/created, the Address will be transformed to ShipmentAddress
    public void setSourceAddress(Address sourceAddress) {
        ShipmentAddress address = new ShipmentAddress(sourceAddress, ShipmentAddressType.SOURCE);
        address.setShipment(this);
        addresses.add(address);
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        ShipmentAddress address = new ShipmentAddress(deliveryAddress, ShipmentAddressType.DESTINATION);
        address.setShipment(this);
        addresses.add(address);
    }
}
