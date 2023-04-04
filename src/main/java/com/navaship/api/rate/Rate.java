package com.navaship.api.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "rate")
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    @Column(name = "id", nullable = false)
    private Long navaRateId;
    @Column(name = "easypost_rate_id", nullable = false)
    @JsonProperty("easyPostRateId")
    private String id;
    @Column(nullable = false)
    private String carrier;
    @Column(nullable = false)
    private BigDecimal rate; // Navaship Rate
    @Column(nullable = false)
    private String currency;
    private String service;
    private String serviceCode;
    private Date deliveryDate;
    private Integer deliveryDays;
    private Integer estDeliveryDays;
    private Boolean deliveryDateGuaranteed;
    private String carrierAccountId;
    private BigDecimal retailRate;
}
