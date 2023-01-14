package com.navaship.api.shipments;

import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
import com.navaship.api.rates.NavaRate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


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

    @ManyToOne
    private Address fromAddress;

    @ManyToOne
    private Address toAddress;

    @ManyToOne
    private Package parcel;

    @OneToOne
    @JoinColumn(name = "rate_id")
    private NavaRate rate;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Gets populated when a rate gets bought for the current shipment
    private String trackingCode;
    private String postageLabelUrl;

    @Column(nullable = false)
    private ShipmentStatus status = ShipmentStatus.DRAFT;

    // Retrieved from easypost webhook
    private EasyPostShipmentStatus easypostShipmentStatus;

    // tracker object
    private String publicTrackingUrl;

    // Serialized as JSON string: contains info about sender and receiver name, company, email and phone
    private String additionalInfoJson;
}
