package com.navaship.api.shipments;

import com.navaship.api.packages.Package;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode(exclude = "shipment")
@NoArgsConstructor
@Entity
public class ShipmentPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Column(nullable = false)
    private BigDecimal weight;
    private BigDecimal value;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    @OneToOne(mappedBy = "parcel")
    private Shipment shipment;


    public ShipmentPackage(Package parcel) {
        this.name = parcel.getName();
        this.weight = parcel.getWeight();
        this.value = parcel.getValue();
        this.length = parcel.getLength();
        this.width = parcel.getWidth();
        this.height = parcel.getHeight();
    }
}
