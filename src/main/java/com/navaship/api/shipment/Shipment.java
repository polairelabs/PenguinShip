package com.navaship.api.shipment;

import com.navaship.api.activity.ActivityLog;
import com.navaship.api.address.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.easypost.EasypostShipmentStatus;
import com.navaship.api.packages.Package;
import com.navaship.api.person.Person;
import com.navaship.api.rate.Rate;
import com.navaship.api.shipmentaddress.ShipmentAddress;
import com.navaship.api.shipmentaddress.ShipmentAddressType;
import com.navaship.api.shipmentpackage.ShipmentPackage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor
@Entity
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
    private EasypostShipmentStatus easypostStatus; // Retrieved from easypost webhook

    // This number represents the "linear id" of the user for his shipments
    @Column(nullable = false)
    private Integer shipmentNumber;

    // Different from the "Rate entity" deliveryDate where this one keeps updating when the status changes
    private Instant deliveryDate;

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

    // Insurance
    private boolean isInsured = false;
    private BigDecimal insuranceAmount;

    // Stripe charge id when Shipment is bought
    private String stripeChargeId;
    // Stripe refund id when Refund has completed
    private String stripeRefundId;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<ActivityLog> activityLogs = new ArrayList<>();


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
