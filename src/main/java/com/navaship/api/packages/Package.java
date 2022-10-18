package com.navaship.api.packages;

import com.navaship.api.appuser.AppUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    private String name;
    private BigDecimal weight;
    private BigDecimal value;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private AppUser user;


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
