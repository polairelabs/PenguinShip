package com.navaship.api.shipments;

import com.navaship.api.addresses.Address;
import com.navaship.api.appuser.AppUser;
import com.navaship.api.packages.Package;
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
    @Column(name = "id")
    private Long id;
    @OneToOne
    @JoinColumn(name = "to_address_id", referencedColumnName = "id")
    private Address toAddress;
    @OneToOne
    @JoinColumn(name = "from_address_id", referencedColumnName = "id")
    private Address fromAddress;
    private BigDecimal amount;
    private String carrier;
    private String trackingNumber;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private AppUser user;
    @OneToOne
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private Package parcel;
}
