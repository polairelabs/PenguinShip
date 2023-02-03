package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.shipments.Shipment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Package {
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
    @ManyToOne
    private AppUser user;
    @OneToMany(mappedBy = "parcel")
    private List<Shipment> shipments = new ArrayList<>();
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Map<String, Object> toPackageMap() {
        Map<String, Object> packageMap = new HashMap<>();
        packageMap.put("name", name);
        packageMap.put("weight", weight);
        packageMap.put("height", height);
        packageMap.put("width", width);
        packageMap.put("length", length);
        return packageMap;
    }
}
