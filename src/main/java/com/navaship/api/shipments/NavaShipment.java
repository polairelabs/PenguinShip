package com.navaship.api.shipments;

import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import com.navaship.api.rates.NavaRate;
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
@Table(name = "shipment")
public class NavaShipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "easypost_shipment_id", nullable = false)
    private String easypostShipmentId;

    @ManyToOne
    private AppUser user;

    @OneToOne
    @JoinColumn(name = "to_address_id", nullable = false)
    private Address toAddress;

    @OneToOne
    @JoinColumn(name = "from_address_id", nullable = false)
    private Address fromAddress;

    @OneToOne
    @JoinColumn(name = "package_id", nullable = false)
    private Package parcel;

    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.DRAFT;

    @OneToOne
    @JoinColumn(name = "rate_id")
    private NavaRate rate;

    // Gets populated when a rate gets bought for the current shipment
    private String trackingCode;
    private String postageLabelUrl;
}
