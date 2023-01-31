package com.navaship.api.shipments;

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
@EqualsAndHashCode
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
    @ManyToOne
    private Address fromAddress;
    @ManyToOne
    private Address toAddress;
    @ManyToOne
    private Package parcel;
    @OneToOne
    @JoinColumn(name = "rate_id")
    private Rate rate;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Gets populated when a rate gets bought for the current shipment
    private String trackingCode;
    private String postageLabelUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.DRAFT;
    // Retrieved from easypost webhook
    @Enumerated(EnumType.STRING)
    private EasyPostShipmentStatus easypostShipmentStatus;

    // Tracker object
    private String publicTrackingUrl;

    // Person contains info about sender and receiver name, company, email and phone
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<Person> persons = new ArrayList<>();
}
