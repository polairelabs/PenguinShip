package com.navaship.api.rates;

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
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "easypost_rate_id", nullable = false)
    private String easypostRateId;
    @Column(nullable = false)
    private String carrier;
    @Column(nullable = false)
    private BigDecimal rate;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private Integer deliveryDays;
    @Column(nullable = false)
    private Integer estDeliveryDays;
    @Column(nullable = false)
    private Boolean deliveryDateGuaranteed;
}
