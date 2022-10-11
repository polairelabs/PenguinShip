package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
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
public class Packages {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private BigDecimal weight;
    private BigDecimal value;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="userId")
    private AppUser appUser;

}
