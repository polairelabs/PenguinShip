package com.navaship.api.shipments;

import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import com.navaship.api.rates.Rate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "easypost_shipment_id", nullable = false)
    private String easypostShipmentId;

    @OneToOne
    @JoinColumn(name = "to_address_id")
    private Address toAddress;

    @OneToOne
    @JoinColumn(name = "from_address_id")
    private Address fromAddress;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @OneToOne
    @JoinColumn(name = "package_id")
    private Package parcel;

    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.DRAFT;

    @OneToOne
    @JoinColumn(name="rate_id")
    private Rate rate;
}
