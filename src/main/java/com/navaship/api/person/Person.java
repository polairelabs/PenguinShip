package com.navaship.api.person;

import com.navaship.api.shipment.Shipment;
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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"shipment_id", "type"})})
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;
    private String name;
    private String company;
    private String phoneNumber;
    private String email;
    @Enumerated(EnumType.STRING)
    private PersonType type;
}
